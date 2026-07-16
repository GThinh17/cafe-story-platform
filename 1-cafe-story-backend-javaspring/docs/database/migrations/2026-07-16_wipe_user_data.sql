-- Wipe all user-generated data for a clean demo.
-- Preserves metadata tables:
--   roles, regions, region_provinces, region_cities, region_wards,
--   report_reasons, reviewer_formula, reviewer_badge_threshold
-- Preserves all table structures and enum types.

BEGIN;

TRUNCATE TABLE
  ad_clicks,
  ad_impressions,
  ad_daily_stats,
  ad_target_regions,
  ad_fees,
  ad_campaigns,

  blog_events,
  blog_daily_metrics,
  blog_recommendation_scores,
  blog_trending_scores,
  blog_ranking_overrides,
  blog_ratings,
  blog_likes,
  blog_saves,
  blog_shares,
  blog_tagged_users,
  blogs,

  chat_messages,
  chat_members,
  conversations,
  comments,

  ai_moderation_results,
  content_reports,

  notifications,

  cafe_page_ratings,
  page_likes,
  page_follows,
  page_members,
  cafe_pages,

  reviewer_ranking_snapshot,
  reviewer_income,
  reviewer_payouts,
  reviewer_badges,
  reviewer_stripe_accounts,
  reviewers,

  payment_details,
  payments,
  extra_fees,
  admin_payout,

  refresh_tokens,
  user_follows,
  user_roles,
  users
RESTART IDENTITY CASCADE;

COMMIT;
