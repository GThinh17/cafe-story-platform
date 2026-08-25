import unittest
from app.utils.json_extractor import extract_json


class ExtractJsonTest(unittest.TestCase):
    def test_bare_json(self):
        raw = '{"is_cafe": true, "confidence": 80, "reason": "ok"}'
        result = extract_json(raw)
        self.assertEqual(result["is_cafe"], True)
        self.assertEqual(result["confidence"], 80)

    def test_json_embedded_in_text(self):
        raw = 'Here is my analysis:\n{"is_cafe": true, "confidence": 75, "reason": "co quan cafe"}'
        result = extract_json(raw)
        self.assertEqual(result["is_cafe"], True)
        self.assertEqual(result["confidence"], 75)

    def test_markdown_json_block(self):
        raw = 'Sure!\n```json\n{"is_cafe": true, "confidence": 80, "reason": "ok"}\n```'
        result = extract_json(raw)
        self.assertEqual(result["is_cafe"], True)

    def test_plain_code_block(self):
        raw = '```\n{"is_cafe": false, "confidence": 20, "reason": "no"}\n```'
        result = extract_json(raw)
        self.assertEqual(result["is_cafe"], False)
        self.assertEqual(result["confidence"], 20)

    def test_garbage_returns_empty(self):
        result = extract_json("Unable to process this request.")
        self.assertEqual(result, {})

    def test_empty_string_returns_empty(self):
        result = extract_json("")
        self.assertEqual(result, {})

    def test_violation_score_fields(self):
        raw = '{"is_coffee_related": true, "is_violation": false, "score": 10, "reason": "ok"}'
        result = extract_json(raw)
        self.assertEqual(result["is_coffee_related"], True)
        self.assertEqual(result["is_violation"], False)
        self.assertEqual(result["score"], 10)


if __name__ == "__main__":
    unittest.main()
