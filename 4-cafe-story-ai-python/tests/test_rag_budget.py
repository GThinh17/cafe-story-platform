import unittest
from unittest.mock import patch


class BudgetByOperationTest(unittest.TestCase):
    """Plan A1: token 'embed' phải tính giá embedding (0.02/1M), KHÔNG phải giá
    generator (0.15/1M). Trước fix, mọi input_tokens bị tính giá generator nên
    một đợt ingest lớn làm tràn budget và khoá toàn bộ chat trong ngày."""

    def _spend(self, by_op):
        with patch("app.services.rag.embedder.get_vector_store") as mock_store:
            mock_store.return_value.usage_today_by_operation.return_value = by_op
            from app.services.rag import embedder
            return embedder.estimated_spend_today_usd(use_cache=False)

    def test_embed_tokens_priced_cheaply(self):
        # 5M token embed = 5 * 0.02 = 0.10 USD.
        spend = self._spend({"embed": (5_000_000, 0)})
        self.assertAlmostEqual(spend, 0.10, places=4)

    def test_embed_much_cheaper_than_generator_for_same_tokens(self):
        embed_spend = self._spend({"embed": (5_000_000, 0)})
        gen_spend = self._spend({"generate": (5_000_000, 0)})
        # generator input 0.15/1M gấp 7.5x embedding.
        self.assertAlmostEqual(gen_spend / embed_spend, 7.5, places=3)

    def test_generate_counts_input_and_output(self):
        # input 1M * 0.15 + output 1M * 0.60 = 0.75 USD.
        spend = self._spend({"generate": (1_000_000, 1_000_000)})
        self.assertAlmostEqual(spend, 0.75, places=4)

    def test_large_embed_does_not_trip_budget(self):
        with patch("app.services.rag.embedder.get_vector_store") as mock_store, \
                patch("app.services.rag.embedder.RAG_DAILY_BUDGET_USD", 1.0):
            mock_store.return_value.usage_today_by_operation.return_value = {
                "embed": (10_000_000, 0)  # 0.20 USD < 1.0
            }
            from app.services.rag import embedder
            embedder._spend_cache = None
            embedder.check_budget()  # không được raise

    def test_generate_over_budget_raises(self):
        with patch("app.services.rag.embedder.get_vector_store") as mock_store, \
                patch("app.services.rag.embedder.RAG_DAILY_BUDGET_USD", 1.0):
            mock_store.return_value.usage_today_by_operation.return_value = {
                "generate": (10_000_000, 0)  # 1.50 USD >= 1.0
            }
            from app.services.rag import embedder
            embedder._spend_cache = None
            with self.assertRaises(embedder.BudgetExceededError):
                embedder.check_budget()


if __name__ == "__main__":
    unittest.main()
