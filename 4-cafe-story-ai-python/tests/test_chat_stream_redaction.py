import json
import types
import unittest
from unittest.mock import patch


class _FakeStore:
    def log_usage(self, *_args):
        return None


class _FakeChatCompletions:
    def create(self, **_kwargs):
        return [
            types.SimpleNamespace(
                choices=[types.SimpleNamespace(delta=types.SimpleNamespace(content="Contact abc@"))]
            ),
            types.SimpleNamespace(
                choices=[types.SimpleNamespace(delta=types.SimpleNamespace(content="example.com now"))]
            ),
        ]


class _FakeClient:
    chat = types.SimpleNamespace(completions=_FakeChatCompletions())


def _event(content):
    return types.SimpleNamespace(
        choices=[types.SimpleNamespace(delta=types.SimpleNamespace(content=content))],
        usage=None,
    )


class _MultiLineChatCompletions:
    """Nội dung nhiều dòng, email x@y.com bị chẻ qua 2 chunk trong cùng 1 dòng."""

    def create(self, **_kwargs):
        return [
            _event("Dòng 1\n"),
            _event("Dòng 2 có mail x@"),
            _event("y.com\n"),
            _event("Dòng 3"),
        ]


class _MultiLineClient:
    chat = types.SimpleNamespace(completions=_MultiLineChatCompletions())


class ChatStreamRedactionTest(unittest.TestCase):

    def test_stream_emits_only_final_sanitized_answer(self):
        from app.services.chat import generator

        prepared = generator._Prepared(
            decision=types.SimpleNamespace(route="B"),
            chunks=[],
            prompt="prompt",
            cacheable=False,
            query_hash="hash",
        )

        with patch("app.services.chat.generator._prepare", return_value=prepared), \
                patch("app.services.chat.generator._get_client", return_value=_FakeClient()), \
                patch("app.services.chat.generator.get_vector_store", return_value=_FakeStore()), \
                patch("app.services.chat.generator.get_rag_config", return_value={"models": {"generator": "test"}}):
            events = list(generator.answer_query_stream("query"))

        payloads = [json.loads(event.removeprefix("data: ").strip()) for event in events]
        self.assertEqual(len(payloads), 2)
        self.assertIn("[email", payloads[0]["delta"])
        self.assertNotIn("abc@example.com", payloads[0]["delta"])
        self.assertTrue(payloads[1]["done"])

    def test_stream_emits_incrementally_and_redacts_split_email(self):
        from app.services.chat import generator

        prepared = generator._Prepared(
            decision=types.SimpleNamespace(route="B"),
            chunks=[],
            prompt="prompt",
            cacheable=False,
            query_hash="hash",
        )

        with patch("app.services.chat.generator._prepare", return_value=prepared), \
                patch("app.services.chat.generator._get_client", return_value=_MultiLineClient()), \
                patch("app.services.chat.generator.get_vector_store", return_value=_FakeStore()), \
                patch("app.services.chat.generator.get_rag_config", return_value={"models": {"generator": "test"}}):
            events = list(generator.answer_query_stream("query"))

        payloads = [json.loads(event.removeprefix("data: ").strip()) for event in events]
        deltas = [p["delta"] for p in payloads if "delta" in p]
        done = [p for p in payloads if p.get("done")]

        # Stream thật: 3 delta (Dòng 1 / Dòng 2 / Dòng 3) + 1 done.
        self.assertEqual(len(deltas), 3)
        self.assertEqual(len(done), 1)
        self.assertEqual(deltas[0], "Dòng 1\n")
        # Email bị chẻ qua chunk nhưng trong cùng 1 dòng → vẫn mask đúng.
        self.assertIn("[email", deltas[1])
        self.assertNotIn("x@y.com", deltas[1])


if __name__ == "__main__":
    unittest.main()
