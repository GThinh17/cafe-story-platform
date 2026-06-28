import pg from 'pg';
const { Client } = pg;

const c = new Client({
  connectionString: 'postgresql://postgres.oafzgtyyttdhocqkyxox:KO4S!^fi=gMrRTGT1704@aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres',
  ssl: { rejectUnauthorized: false }
});

await c.connect();

const res = await c.query(`
  SELECT rb.id, rb.reviewer_id, rb.badge_month, rb.score, rb.badge,
         rb.like_count, rb.share_count, rb.comment_count,
         rb.created_at, rb.updated_at,
         u.user_name, u.user_email
  FROM reviewer_badges rb
  JOIN reviewers r ON r.reviewer_id = rb.reviewer_id
  JOIN users u ON u.user_id = r.user_id
  WHERE u.user_name = 'zdexios' OR u.user_email = 'zdexios@gmail.com'
  ORDER BY rb.badge_month DESC
`);

console.log('=== reviewer_badges ===');
console.log(JSON.stringify(res.rows, null, 2));

const snap = await c.query(`
  SELECT s.id, s.reviewer_id, s.period, s.period_type, s.rank_position,
         s.score, s.badge, s.like_count, s.share_count, s.comment_count,
         s.created_at, s.updated_at,
         u.user_name
  FROM reviewer_ranking_snapshot s
  JOIN reviewers r ON r.reviewer_id = s.reviewer_id
  JOIN users u ON u.user_id = r.user_id
  WHERE (u.user_name = 'zdexios' OR u.user_email = 'zdexios@gmail.com')
    AND s.period_type = 'MONTHLY'
  ORDER BY s.period DESC
`);

console.log('=== reviewer_ranking_snapshot (MONTHLY) ===');
console.log(JSON.stringify(snap.rows, null, 2));

await c.end();
