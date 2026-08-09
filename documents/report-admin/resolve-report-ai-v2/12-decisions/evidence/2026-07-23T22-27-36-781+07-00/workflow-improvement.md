# Workflow Improvement

1. Protected list phải dùng stable code và version, không hard-code trong prompt.
2. Attack ngoài protected list phải có explicit fallback, không silently ignore.
3. Test cần cover quotation, counter-speech, reclaimed speech và identity inference guard.
