"""
Port của AuthServiceImpl.generateUserNameCandidates (Java) sang Python.
Giữ đúng semantics: cùng full_name → cùng list candidates → cùng username được chọn.

Constants + regex phải khớp Java (VALID_USERNAME_PATTERN, MIN/MAX_USERNAME_LENGTH,
RESERVED_USER_NAMES, MAX_CANDIDATE_COUNT).
"""

from __future__ import annotations

import re
import unicodedata
from typing import Iterable, List, Optional, Set

MIN_USERNAME_LENGTH = 5
MAX_USERNAME_LENGTH = 10
MIN_SUGGESTION_COUNT = 4
MAX_SUGGESTION_COUNT = 7
MAX_CANDIDATE_COUNT = 25
VALID_USERNAME_PATTERN = re.compile(r"^[a-z0-9](?!.*[._]{2})[a-z0-9._]{3,8}[a-z0-9]$")
RESERVED_USER_NAMES: Set[str] = {
    "admin", "root", "login", "register", "api", "me", "support", "cafestory",
}


def _remove_vietnamese_diacritics(value: str) -> str:
    value = value.replace("đ", "d").replace("Đ", "D")
    return "".join(
        c for c in unicodedata.normalize("NFD", value)
        if unicodedata.category(c) != "Mn"
    )


def _normalize_for_seed(full_name: str) -> str:
    return _remove_vietnamese_diacritics(full_name.strip().lower())


def _normalize_candidate(candidate: str) -> str:
    normalized = _remove_vietnamese_diacritics(candidate.lower())
    normalized = re.sub(r"[^a-z0-9._]", "", normalized)
    normalized = re.sub(r"[._]{2,}", "_", normalized)
    normalized = re.sub(r"^[._]+|[._]+$", "", normalized)
    if len(normalized) > MAX_USERNAME_LENGTH:
        normalized = normalized[:MAX_USERNAME_LENGTH]
        normalized = re.sub(r"[._]+$", "", normalized)
    return normalized


def _normalize_name_tokens(full_name: str) -> List[str]:
    normalized = _remove_vietnamese_diacritics(full_name.strip().lower())
    normalized = re.sub(r"[^a-z0-9\s._]", " ", normalized)
    normalized = re.sub(r"[._]+", " ", normalized).strip()
    if not normalized:
        return ["user"]
    return [token for token in normalized.split() if token]


def _abbreviate(value: str, max_length: int) -> str:
    return value if len(value) <= max_length else value[:max_length]


def _remove_inner_vowels(value: str) -> str:
    if len(value) <= MIN_USERNAME_LENGTH:
        return value
    result: List[str] = []
    for index, ch in enumerate(value):
        if index == 0 or index == len(value) - 1 or ch not in "aeiou":
            result.append(ch)
    return "".join(result)


def _floor_mod(a: int, b: int) -> int:
    """Java's Math.floorMod semantics (differs from Python % for negatives)."""
    return a - (a // b) * b


def _java_string_hash_code(s: str) -> int:
    """Java's String.hashCode(): 32-bit signed int, formula h = 31*h + ch."""
    h = 0
    for ch in s:
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    if h >= 0x80000000:
        h -= 0x100000000
    return h


def _suffix(seed: int, attempt: int) -> str:
    number = _floor_mod(seed + attempt * 17, 997)
    if number < 2:
        number += 2
    return f"_{number}"


def _add_candidate(candidates: List[str], candidate: str) -> None:
    if len(candidates) >= MAX_CANDIDATE_COUNT:
        return
    normalized = _normalize_candidate(candidate)
    if normalized and normalized not in candidates:
        candidates.append(normalized)


def _add_candidate_with_suffix(candidates: List[str], base: str, sfx: str) -> None:
    base_max_length = MAX_USERNAME_LENGTH - len(sfx)
    shortened = _normalize_candidate(base)
    if len(shortened) > base_max_length:
        shortened = shortened[:base_max_length]
        shortened = re.sub(r"[._]+$", "", shortened)
    _add_candidate(candidates, shortened + sfx)


def is_valid_user_name(candidate: str) -> bool:
    return (
        MIN_USERNAME_LENGTH <= len(candidate) <= MAX_USERNAME_LENGTH
        and VALID_USERNAME_PATTERN.match(candidate) is not None
    )


def generate_username_candidates(full_name: str) -> List[str]:
    tokens = _normalize_name_tokens(full_name)
    first = tokens[0] if tokens else "user"
    last = tokens[-1] if tokens else "user"
    penultimate = tokens[-2] if len(tokens) > 1 else ""
    joined = "".join(tokens)
    condensed_joined = _remove_inner_vowels(joined)
    seed = _floor_mod(_java_string_hash_code(_normalize_for_seed(full_name)), 0x7FFFFFFF)

    candidates: List[str] = []
    _add_candidate(candidates, first + "." + last)
    _add_candidate(candidates, first + "_" + last)
    _add_candidate(candidates, first + last)
    _add_candidate(candidates, last + "." + first)
    _add_candidate(candidates, _abbreviate(first, 4) + last)
    _add_candidate(candidates, first + _abbreviate(last, 4))

    if penultimate and penultimate != first:
        _add_candidate(candidates, penultimate + "." + last)
        _add_candidate(candidates, penultimate + "_" + last)
        _add_candidate(candidates, penultimate + last)
        _add_candidate(candidates, first + _abbreviate(penultimate, 1) + "_" + last)
        _add_candidate(candidates, _abbreviate(penultimate, 4) + last + _suffix(seed, 2))

    _add_candidate(candidates, joined)
    _add_candidate(candidates, condensed_joined)
    _add_candidate(candidates, condensed_joined + _suffix(seed, 2))
    _add_candidate(candidates, _abbreviate(joined, MAX_USERNAME_LENGTH))

    attempt = 0
    while len(candidates) < MAX_CANDIDATE_COUNT and attempt < 500:
        base = "user" if len(candidates) < MIN_SUGGESTION_COUNT else joined
        if not base:
            base = "user"
        _add_candidate_with_suffix(
            candidates, base, _suffix(seed + attempt * 31, attempt)
        )
        attempt += 1

    return candidates


def pick_username(full_name: str, taken: Iterable[str]) -> Optional[str]:
    """First valid, non-reserved, non-taken candidate — mirrors Java suggestUserNames flow."""
    taken_set = set(taken)
    for candidate in generate_username_candidates(full_name):
        if (
            is_valid_user_name(candidate)
            and candidate not in RESERVED_USER_NAMES
            and candidate not in taken_set
        ):
            return candidate
    return None
