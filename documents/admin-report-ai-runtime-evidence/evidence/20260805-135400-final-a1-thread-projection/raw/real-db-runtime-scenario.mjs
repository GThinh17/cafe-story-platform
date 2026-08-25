import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
const require = createRequire(import.meta.url);
const evidenceDir = process.env.EVIDENCE_DIR;
const rawDir = path.join(evidenceDir, "raw");
const screensDir = path.join(evidenceDir, "screenshots");
const repo = process.env.REPO_DIR;
const adminDir = path.join(repo, "5-cafe-story-nextjs-admin");
const apiBase = "http://localhost:8080";
const adminBase = "http://localhost:3636";
const marker = `REAL-AI-${new Date().toISOString().replace(/[-:.TZ]/g, "").slice(0, 14)}`;
const startedAt = new Date().toISOString();
const authorLogin = { identifier: "gthnh_170", password: "123456" };
const adminLogin = { identifier: "thinh@gmail.com", password: "123456" };
const realImages = {
  normal: "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=1200&q=80",
  severe: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80",
  caption: "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=80",
  parent: "https://images.unsplash.com/photo-1442512595331-e89e73853f31?auto=format&fit=crop&w=1200&q=80"
};
const result = { marker, startedAt, apiBase, adminBase, scope: { author: "gthnh_170", reporterAdmin: "thinh@gmail.com", database: "current backend DB", imagePolicy: "real public image URLs, backend metadata only" }, users: {}, region: null, reason: null, cases: [], screenshots: [], errors: [] };
function unwrap(raw){ return raw && typeof raw === "object" && Object.prototype.hasOwnProperty.call(raw, "data") ? raw.data : raw; }
function sanitize(v){ if(Array.isArray(v)) return v.map(sanitize); if(!v || typeof v !== "object") return v; const out={}; for(const [k,val] of Object.entries(v)){ out[k] = /token|password|secret|api[_-]?key/i.test(k) ? "[REDACTED]" : sanitize(val); } return out; }
function esc(s){ return String(s ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;","'":"&#39;"}[c])); }
async function api(method, pathName, body, token, expectOk=true, timeoutMs=160000){
  const ac = new AbortController(); const timer = setTimeout(() => ac.abort(), timeoutMs);
  try {
    const headers = { Accept: "application/json" };
    const init = { method, headers, signal: ac.signal };
    if(token) headers.Authorization = `Bearer ${token}`;
    if(body !== undefined && body !== null){ headers["Content-Type"] = "application/json"; init.body = JSON.stringify(body); }
    const res = await fetch(`${apiBase}${pathName}`, init);
    const text = await res.text(); let raw = null;
    if(text){ try { raw = JSON.parse(text); } catch { raw = { text }; } }
    if(expectOk && !res.ok) throw new Error(`${method} ${pathName} -> HTTP ${res.status}: ${text.slice(0, 1000)}`);
    return { ok: res.ok, status: res.status, raw, data: unwrap(raw) };
  } finally { clearTimeout(timer); }
}
function writeJson(name, data){ fs.writeFileSync(path.join(rawDir, name), JSON.stringify(sanitize(data), null, 2), "utf8"); }
async function login(credentials){ return (await api("POST", "/api/auth/login", credentials, null, true)).data; }
async function createRegion(adminToken){
  const provinces = unwrap(await (await fetch(`${apiBase}/api/regions/provinces`)).json()) ?? [];
  if(!provinces.length) throw new Error("No provinces returned by /api/regions/provinces");
  let selected = null;
  for(const province of provinces){
    const citiesRaw = await (await fetch(`${apiBase}/api/regions/cities?provinceCode=${encodeURIComponent(province.provinceCode)}`)).json();
    const cities = unwrap(citiesRaw) ?? [];
    if(cities.length){ selected = { province, city: cities[0] }; break; }
  }
  if(!selected) throw new Error("No city matched available provinces");
  const body = { provinceCode: selected.province.provinceCode, cityCode: selected.city.cityCode, area: `Runtime Evidence Area ${marker}` };
  return (await api("POST", "/api/regions?requirement=BLOG_LOCATION", body, adminToken, true)).data;
}
async function getReason(type, token){
  const reasons = (await api("GET", `/api/report-reasons?targetType=${type}`, null, token, true)).data ?? [];
  return reasons.find(r => r.code === "SCAM_FRAUD_OR_SPAM") ?? reasons.sort((a,b)=>(b.severity ?? 0)-(a.severity ?? 0))[0];
}
async function createBlog(token, regionId, content, imageUrl){ return (await api("POST", "/api/blogs", { regionId, content, imageUrls: [imageUrl], allowComment: true, isPinned: false }, token, true)).data; }
async function createComment(token, blogId, content){ return (await api("POST", "/api/comments", { blogId, content, imageUrls: [] }, token, true)).data; }
async function reportTarget(token, targetType, targetId, reasonId, label){ return (await api("POST", "/api/reports", { targetType, targetId, reasonId, description: `[${marker}] Runtime report by thinh@gmail.com for ${label}` }, token, true)).data; }
async function getReport(token, reportId){ return (await api("GET", `/api/admin/reports/${reportId}`, null, token, true)).data; }
async function getTarget(token, type, id){ return type === "BLOG" ? (await api("GET", `/api/admin/blogs/${id}`, null, token, true)).data : (await api("GET", `/api/admin/comments/${id}`, null, token, true)).data; }
async function getJobs(token, reportId){ return ((await api("GET", `/api/admin/reports/${reportId}/ai-auto-resolutions?page=0&size=20`, null, token, true)).data?.content) ?? []; }
function reasonSummary(c){ const a=c.ai ?? {}; const bits=[`AI decision=${a.reportDecision ?? "N/A"}`, `targetAction=${a.targetAction ?? "N/A"}`, `evidence=${a.evidenceQuality ?? "N/A"}/${a.evidenceSufficiency ?? "N/A"}`, `likelihood=${a.violationLikelihood ?? "N/A"}`, `automationMode=${a.automationMode ?? "N/A"}`]; if(a.autoApplyWarning) bits.push(`warning=${a.autoApplyWarning}`); if(a.blockedReasons?.length) bits.push(`blocked=${a.blockedReasons.join(",")}`); if(a.explanation) bits.push(String(a.explanation).replace(/\s+/g," ").slice(0,260)); return bits.join("; "); }
async function writeArtifacts(){
  writeJson("runtime-results.json", result);
  const rows = result.cases.map(c => `<tr><td>${esc(c.label)}</td><td>${esc(c.type)}</td><td><a href="${esc(c.imageUrl ?? "")}">${esc(c.imageUrl ? "real image" : "n/a")}</a></td><td>${esc(c.ai?.automationMode ?? "N/A")}</td><td>${esc(c.ai?.reportDecision ?? "N/A")}</td><td>${esc(c.ai?.targetAction ?? "N/A")}</td><td>${esc(`${c.ai?.evidenceQuality ?? "N/A"}/${c.ai?.evidenceSufficiency ?? "N/A"}/${c.ai?.violationLikelihood ?? "N/A"}`)}</td><td>${esc((c.jobsFinal ?? []).map(j => j.status).join(", ") || "none")}</td><td>${esc(c.finalReport?.status ?? "N/A")}</td><td>${esc(c.finalTarget?.status ?? "N/A")}</td><td class="${c.hidden ? "bad" : "ok"}">${c.hidden ? "YES" : "NO"}</td><td>${esc(c.reasonSummary ?? "")}</td></tr>`).join("\n");
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>CafeStory Runtime AI Evidence</title><style>body{font-family:Arial,sans-serif;margin:28px;background:#f7f8fa;color:#17202a}h1{font-size:24px;margin:0 0 8px}.pill{display:inline-block;background:#fff;border:1px solid #d8dde3;border-radius:6px;padding:7px 10px;margin:3px;font-size:12px}table{border-collapse:collapse;width:100%;background:#fff;margin-top:16px}th,td{border:1px solid #e2e6ea;padding:8px;font-size:12px;vertical-align:top;line-height:1.35}th{background:#263238;color:white;text-align:left}.ok{color:#116329;font-weight:700}.bad{color:#b42318;font-weight:700}.status{font-weight:700;color:${result.errors.length ? "#b42318" : "#116329"}}</style></head><body><h1>CafeStory Admin Report AI Runtime Evidence</h1><div class="status">${result.errors.length ? "PARTIAL/BLOCKED" : "Scenario completed"}</div><div><span class="pill">Marker: ${esc(marker)}</span><span class="pill">Author: gthnh_170</span><span class="pill">Reporter/Admin: thinh@gmail.com</span><span class="pill">Backend DB: current running DB</span><span class="pill">n8n: localhost:5678</span></div><table><thead><tr><th>Case</th><th>Type</th><th>Image</th><th>Mode</th><th>Decision</th><th>Action</th><th>Evidence</th><th>Auto job</th><th>Report</th><th>Target</th><th>Hidden?</th><th>Observed reason</th></tr></thead><tbody>${rows || `<tr><td colspan="12">${esc(result.errors.map(e=>e.message).join("; "))}</td></tr>`}</tbody></table></body></html>`;
  fs.writeFileSync(path.join(evidenceDir, "runtime-summary.html"), html, "utf8");
  const mdRows = result.cases.map(c => `| ${c.label} | ${c.type} | ${c.imageUrl ? "YES" : "NO"} | ${c.ai?.automationMode ?? "N/A"} | ${c.ai?.reportDecision ?? "N/A"} | ${c.ai?.targetAction ?? "N/A"} | ${c.ai?.evidenceQuality ?? "N/A"}/${c.ai?.evidenceSufficiency ?? "N/A"}/${c.ai?.violationLikelihood ?? "N/A"} | ${(c.jobsFinal ?? []).map(j=>j.status).join(", ") || "none"} | ${c.finalReport?.status ?? "N/A"} | ${c.finalTarget?.status ?? "N/A"} | ${c.hidden ? "YES" : "NO"} | ${String(c.reasonSummary ?? "").replace(/\|/g,"/")} |`).join("\n");
  const md = `# CafeStory Admin Report AI Runtime Test Evidence\n\n- Timestamp: ${startedAt}\n- Backend: ${apiBase}\n- Admin UI: ${adminBase}\n- Author account: gthnh_170\n- Reporter/admin account: thinh@gmail.com\n- Database: current backend DB opened by user\n- Marker: ${marker}\n- Image evidence: real public image URLs saved in blog imageUrls; backend AI evidence remains PLATFORM_URL_METADATA_ONLY.\n\n## Result Table\n\n| Case | Type | Real image? | Mode | AI decision | Target action | Evidence quality/sufficiency/likelihood | Auto job final | Report final | Target final | Hidden? | Observed reason |\n|---|---|---|---|---|---|---|---|---|---|---|---|\n${mdRows || "| N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | Scenario did not create cases |"}\n\n## Screenshots\n\n${result.screenshots.map(s=>`- ${s}`).join("\n") || "- Screenshot capture pending/failed."}\n\n## Raw Evidence\n\n- raw/runtime-results.json\n- runtime-summary.html\n\n## Errors\n\n${result.errors.length ? result.errors.map(e=>`- ${e.message}`).join("\n") : "- None recorded by scenario runner."}\n`;
  fs.writeFileSync(path.join(evidenceDir, "bao-cao-danh-gia.md"), md, "utf8");
  fs.writeFileSync(path.join(evidenceDir, "issue.md"), result.errors.length ? result.errors.map(e=>`- ${e.message}`).join("\n") + "\n" : "No runtime issue recorded by this run.\n", "utf8");
  fs.writeFileSync(path.join(evidenceDir, "fix-log.md"), "No source fix applied during real DB runtime evidence run.\n", "utf8");
  fs.writeFileSync(path.join(evidenceDir, "workflow-improvement.md"), "For repeatability, add a checked-in runtime harness that accepts author/reporter credentials and an existing region strategy.\n", "utf8");
  fs.writeFileSync(path.join(evidenceDir, "summary.json"), JSON.stringify(sanitize({ status: result.errors.length ? "PARTIAL" : "DONE_CANDIDATE", evidenceDir, marker, caseCount: result.cases.length, screenshots: result.screenshots, errors: result.errors }), null, 2), "utf8");
}
async function captureScreenshots(adminToken){
  try {
    const { chromium } = require(path.join(adminDir, "node_modules", "playwright"));
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage({ viewport: { width: 1440, height: 1100 }, baseURL: adminBase });
    await page.goto("file:///" + path.join(evidenceDir, "runtime-summary.html").replace(/\\/g,"/"));
    await page.screenshot({ path: path.join(screensDir, "01-runtime-ai-summary.png"), fullPage: true });
    result.screenshots.push("screenshots/01-runtime-ai-summary.png");
    await page.goto("/login", { waitUntil: "networkidle", timeout: 45000 });
    await page.getByPlaceholder("Email or username").fill(adminLogin.identifier);
    await page.getByPlaceholder("Password").fill(adminLogin.password);
    await page.getByRole("button", { name: "Sign in" }).click();
    await page.waitForTimeout(2500);
    await page.goto("/reports", { waitUntil: "networkidle", timeout: 45000 });
    await page.screenshot({ path: path.join(screensDir, "02-admin-reports-ui.png"), fullPage: true });
    result.screenshots.push("screenshots/02-admin-reports-ui.png");
    await browser.close();
  } catch (error) {
    result.screenshotError = error?.message ?? String(error);
  }
}
try {
  const authorAuth = await login(authorLogin);
  const adminAuth = await login(adminLogin);
  const authorToken = authorAuth.accessToken;
  const adminToken = adminAuth.accessToken;
  result.users = { author: { userName: authorAuth.user?.userName, userId: authorAuth.user?.userId }, admin: { email: adminAuth.user?.userEmail, userId: adminAuth.user?.userId, roles: adminAuth.user?.roles ?? [] } };
  const region = await createRegion(adminToken);
  result.region = region;
  const blogReason = await getReason("BLOG", adminToken);
  const commentReason = await getReason("COMMENT", adminToken);
  result.reason = { blogReason, commentReason };
  const normalBlog = await createBlog(authorToken, region.regionId, `[${marker}] Normal cafe post: cappuccino tasting notes, calm workspace, no suspicious link, no money request, no harassment.`, realImages.normal);
  const severeBlog = await createBlog(authorToken, region.regionId, `[${marker}] Severe scam blog: send your bank OTP, transfer money now, guaranteed 500 percent profit, fake investment group, phishing login http://phishing.invalid/bank.`, realImages.severe);
  const captionScamBlog = await createBlog(authorToken, region.regionId, `[${marker}] Cafe photo with scam caption: the image is normal coffee, but caption says transfer money now for fake voucher, guaranteed profit, click phishing link http://scam.invalid/voucher.`, realImages.caption);
  const parentBlog = await createBlog(authorToken, region.regionId, `[${marker}] Parent blog for comment report context: normal cafe discussion thread.`, realImages.parent);
  await createComment(authorToken, parentBlog.id, `[${marker}] Sibling before: normal friendly coffee comment.`);
  const normalComment = await createComment(authorToken, parentBlog.id, `[${marker}] Normal comment: this coffee looks good and the space is quiet.`);
  const severeComment = await createComment(authorToken, parentBlog.id, `[${marker}] Severe scam comment: send bank OTP, transfer money now, click http://phishing.invalid/prize to claim fake reward.`);
  await createComment(authorToken, parentBlog.id, `[${marker}] Sibling after: normal follow-up with no suspicious request.`);
  const defs = [
    { key: "BLOG_NORMAL", label: "Blog normal", type: "BLOG", targetId: normalBlog.id, imageUrl: realImages.normal, reasonId: blogReason.id },
    { key: "BLOG_SEVERE", label: "Blog severe scam", type: "BLOG", targetId: severeBlog.id, imageUrl: realImages.severe, reasonId: blogReason.id },
    { key: "BLOG_CAPTION_SCAM", label: "Blog image with scam caption", type: "BLOG", targetId: captionScamBlog.id, imageUrl: realImages.caption, reasonId: blogReason.id },
    { key: "COMMENT_NORMAL", label: "Comment normal", type: "COMMENT", targetId: normalComment.id, imageUrl: null, reasonId: commentReason.id },
    { key: "COMMENT_SEVERE", label: "Comment severe scam", type: "COMMENT", targetId: severeComment.id, imageUrl: null, reasonId: commentReason.id, parentBlogId: parentBlog.id }
  ];
  for (const d of defs) {
    const report = await reportTarget(adminToken, d.type, d.targetId, d.reasonId, d.label);
    const beforeTarget = await getTarget(adminToken, d.type, d.targetId);
    const beforeReport = await getReport(adminToken, report.id);
    const start = Date.now();
    const aiResp = await api("POST", `/api/admin/reports/${report.id}/ai-resolution`, {}, adminToken, false, 180000);
    const c = { ...d, report, beforeReport, beforeTarget, aiHttpStatus: aiResp.status, aiDurationMs: Date.now() - start, ai: aiResp.ok ? aiResp.data : null, aiError: aiResp.ok ? null : aiResp.raw, jobsInitial: await getJobs(adminToken, report.id) };
    result.cases.push(c);
    await writeArtifacts();
  }
  const deadline = Date.now() + 380000;
  while (Date.now() < deadline) {
    let pending = false;
    for (const c of result.cases) {
      c.jobsFinal = await getJobs(adminToken, c.report.id);
      if (c.jobsFinal.some(j => ["SCHEDULED", "APPLYING"].includes(j.status))) pending = true;
    }
    if (!pending) break;
    await new Promise(r => setTimeout(r, 5000));
  }
  for (const c of result.cases) {
    c.finalReport = await getReport(adminToken, c.report.id);
    c.finalTarget = await getTarget(adminToken, c.type, c.targetId);
    c.jobsFinal = await getJobs(adminToken, c.report.id);
    c.hidden = c.finalTarget?.status === "HIDDEN";
    if (c.parentBlogId) c.parentBlogAfter = await getTarget(adminToken, "BLOG", c.parentBlogId);
    c.reasonSummary = reasonSummary(c);
  }
  result.completedAt = new Date().toISOString();
  await writeArtifacts();
  await captureScreenshots(adminToken);
  await writeArtifacts();
} catch (error) {
  result.errors.push({ message: error?.message ?? String(error), stack: error?.stack ?? null });
  result.completedAt = new Date().toISOString();
  await writeArtifacts();
  await captureScreenshots(null);
  await writeArtifacts();
}
console.log(JSON.stringify({ evidenceDir, marker, caseCount: result.cases.length, screenshots: result.screenshots, errors: result.errors.map(e=>e.message), modes: result.cases.map(c=>c.ai?.automationMode), hidden: result.cases.map(c=>({key:c.key, target:c.finalTarget?.status, report:c.finalReport?.status, hidden:c.hidden, jobs:(c.jobsFinal??[]).map(j=>j.status)})) }, null, 2));
