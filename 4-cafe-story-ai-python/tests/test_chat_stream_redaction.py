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


if __name__ == "__main__":
    unittest.main()
