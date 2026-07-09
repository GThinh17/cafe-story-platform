import unittest
from unittest.mock import MagicMock, patch


class RouteQueryTest(unittest.TestCase):

    def _route(self, llm_response: str, query: str = "cafe Cần Thơ", history=None):
        with patch("app.services.rag.router._get_router_model") as mock_model, \
                patch("app.services.rag.router.get_vector_store") as mock_store:
            mock_model.return_value.generate_text.return_value = llm_response
            mock_store.return_value.log_usage = MagicMock()
            from app.services.rag.router import route_query

            return route_query(query, history)

    def test_parses_route_and_province(self):
        decision = self._route(
            '{"route": "B", "rewritten_query": "quán cafe ở Cần Thơ", "province": "Cần Thơ"}'
        )
        self.assertEqual(decision.route, "B")
        self.assertEqual(decision.rewritten_query, "quán cafe ở Cần Thơ")
        self.assertEqual(decision.province, "Cần Thơ")

    def test_null_province_normalized(self):
        decision = self._route('{"route": "C", "rewritten_query": "gói reviewer", "province": "null"}')
        self.assertIsNone(decision.province)

    def test_invalid_route_falls_back_to_b(self):
        decision = self._route('{"route": "Z", "rewritten_query": "x", "province": null}')
        self.assertEqual(decision.route, "B")

    def test_llm_error_falls_back(self):
        with patch("app.services.rag.router._get_router_model") as mock_model:
            mock_model.return_value.generate_text.side_effect = RuntimeError("boom")
            from app.services.rag.router import route_query

            decision = route_query("cafe sữa ngon")
        self.assertEqual(decision.route, "B")
        self.assertEqual(decision.rewritten_query, "cafe sữa ngon")

    def test_empty_rewrite_keeps_original(self):
        decision = self._route('{"route": "D", "rewritten_query": "", "province": null}', query="đổi avatar")
        self.assertEqual(decision.rewritten_query, "đổi avatar")


class RouteDispatchTest(unittest.TestCase):

    def test_route_a_reviewer_uses_top_reviewers_tool(self):
        from app.services.rag.router import RouteDecision

        with patch("app.services.chat.generator.fetch_top_reviewers") as mock_tool:
            mock_tool.return_value = [
                {"reviewerId": "r1", "userName": "minh", "userFullName": "Minh Tran", "followerCount": 99}
            ]
            from app.services.chat.generator import _retrieve_for_route

            chunks = _retrieve_for_route(
                RouteDecision(route="A", rewritten_query="reviewer uy tín nhất")
            )
        self.assertEqual(len(chunks), 1)
        self.assertEqual(chunks[0].source_type, "reviewer")
        self.assertIn("Minh Tran", chunks[0].content)

    def test_route_a_trending_passes_province(self):
        from app.services.rag.router import RouteDecision

        with patch("app.services.chat.generator.fetch_trending_cafes") as mock_tool:
            mock_tool.return_value = []
            from app.services.chat.generator import _retrieve_for_route

            _retrieve_for_route(
                RouteDecision(route="A", rewritten_query="quán trending Đà Nẵng", province="Đà Nẵng")
            )
        mock_tool.assert_called_once_with(limit=5, province="Đà Nẵng")

    def test_route_b_filters_province(self):
        from app.services.rag.router import RouteDecision

        with patch("app.services.chat.generator.embed_query") as mock_embed, \
                patch("app.services.chat.generator.get_vector_store") as mock_store:
            mock_embed.return_value = [0.0] * 4
            mock_store.return_value.hybrid_search.return_value = []
            from app.services.chat.generator import _retrieve_for_route

            _retrieve_for_route(
                RouteDecision(route="B", rewritten_query="cafe Cần Thơ", province="Cần Thơ")
            )
            call_kwargs = mock_store.return_value.hybrid_search.call_args.kwargs
        self.assertEqual(call_kwargs["metadata_filter"], {"province": "Cần Thơ"})
        self.assertIn("cafe_page", call_kwargs["source_types"])

    def test_route_e_with_jwt_uses_personal_context(self):
        from app.services.rag.router import RouteDecision

        with patch("app.services.chat.generator.fetch_my_blog_moderation") as mock_fetch:
            mock_fetch.return_value = [
                {
                    "blogId": "b1",
                    "blogStatus": "HIDDEN",
                    "decision": "VIOLATION",
                    "captionReason": "Khong lien quan cafe",
                    "imageReason": None,
                    "captionSnippet": "noi dung...",
                }
            ]
            from app.services.chat.generator import _retrieve_for_route

            chunks = _retrieve_for_route(
                RouteDecision(route="E", rewritten_query="tai sao bai cua toi bi tu choi"),
                user_jwt="jwt-token",
            )
        self.assertEqual(len(chunks), 1)
        self.assertEqual(chunks[0].source_type, "user_moderation")
        self.assertIn("Khong lien quan cafe", chunks[0].content)

    def test_route_e_without_jwt_falls_back_to_docs(self):
        from app.services.rag.router import RouteDecision

        with patch("app.services.chat.generator.embed_query") as mock_embed, \
                patch("app.services.chat.generator.get_vector_store") as mock_store:
            mock_embed.return_value = [0.0] * 4
            mock_store.return_value.hybrid_search.return_value = []
            from app.services.chat.generator import _retrieve_for_route

            _retrieve_for_route(
                RouteDecision(route="E", rewritten_query="tai sao bai bi tu choi")
            )
            call_kwargs = mock_store.return_value.hybrid_search.call_args.kwargs
        self.assertEqual(call_kwargs["source_types"], ["doc"])

    def test_route_c_uses_docs_only(self):
        from app.services.rag.router import RouteDecision

        with patch("app.services.chat.generator.embed_query") as mock_embed, \
                patch("app.services.chat.generator.get_vector_store") as mock_store:
            mock_embed.return_value = [0.0] * 4
            mock_store.return_value.hybrid_search.return_value = []
            from app.services.chat.generator import _retrieve_for_route

            _retrieve_for_route(RouteDecision(route="C", rewritten_query="gói reviewer là gì"))
            call_kwargs = mock_store.return_value.hybrid_search.call_args.kwargs
        self.assertEqual(call_kwargs["source_types"], ["doc"])


if __name__ == "__main__":
    unittest.main()
