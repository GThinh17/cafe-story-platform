import type { Locale } from "@/features/i18n";

type LocalizedPolicyCopy = {
  title: string;
  summary: string;
};

type PolicyCopy = {
  en: LocalizedPolicyCopy;
  vi: LocalizedPolicyCopy;
};

export const ADMIN_REPORT_AI_RULE_IDS = [
  "CSR.ROUTE.001",
  "CSR.ROUTE.002",
  "CSR.ROUTE.003",
  "CSR.SPAM.001",
  "CSR.HAR.001",
  "CSR.HAR.002",
  "CSR.SAF.001",
  "CSR.SAF.002",
  "CSR.SAF.003",
  "CSR.HATE.001",
  "CSR.INT.001",
  "CSR.INT.002",
  "CSR.INT.003",
  "CSR.SEX.001",
  "CSR.SEX.002",
  "CSR.PRIV.001",
  "CSR.IP.001",
  "CSR.IP.002",
  "CSR.COM.001",
  "CSR.REL.001",
] as const;

export type AdminReportAiRuleId = (typeof ADMIN_REPORT_AI_RULE_IDS)[number];

const policyCopy: Record<AdminReportAiRuleId, PolicyCopy> = {
  "CSR.ROUTE.001": {
    en: { title: "Preference-only report", summary: "Routes reports that express personal dislike without describing an observable policy violation." },
    vi: { title: "Báo cáo theo sở thích", summary: "Định tuyến báo cáo chỉ thể hiện sự không thích cá nhân mà không mô tả vi phạm chính sách có thể quan sát." },
  },
  "CSR.ROUTE.002": {
    en: { title: "General manual routing", summary: "Routes reports whose stated reason does not map to a more specific active violation rule." },
    vi: { title: "Định tuyến thủ công chung", summary: "Định tuyến báo cáo có lý do chưa khớp với một quy tắc vi phạm cụ thể đang hoạt động." },
  },
  "CSR.ROUTE.003": {
    en: { title: "Media review routing", summary: "Routes image-dependent reports when media references require separate visual verification." },
    vi: { title: "Định tuyến kiểm tra hình ảnh", summary: "Định tuyến báo cáo phụ thuộc hình ảnh khi tham chiếu media cần được kiểm chứng trực quan riêng." },
  },
  "CSR.SPAM.001": {
    en: { title: "Spam and deceptive solicitation", summary: "Covers repeated, unsolicited, or deceptive calls to click links, claim rewards, or join suspicious schemes." },
    vi: { title: "Spam và lời mời chào gian dối", summary: "Áp dụng cho lời kêu gọi lặp lại, không mong muốn hoặc gian dối nhằm nhấp liên kết, nhận thưởng hay tham gia chương trình đáng ngờ." },
  },
  "CSR.HAR.001": {
    en: { title: "Targeted harassment", summary: "Covers abusive or degrading content directed at an identifiable person or account." },
    vi: { title: "Quấy rối có mục tiêu", summary: "Áp dụng cho nội dung lăng mạ hoặc hạ nhục nhắm vào cá nhân hay tài khoản có thể xác định." },
  },
  "CSR.HAR.002": {
    en: { title: "Bullying or unwanted contact", summary: "Covers persistent intimidation, bullying, or unwanted contact directed at a specific target." },
    vi: { title: "Bắt nạt hoặc liên hệ không mong muốn", summary: "Áp dụng cho hành vi đe dọa, bắt nạt hoặc liên hệ dai dẳng không mong muốn nhắm vào mục tiêu cụ thể." },
  },
  "CSR.SAF.001": {
    en: { title: "Credible violence threat", summary: "Covers credible statements of intent to physically harm a person, group, or place." },
    vi: { title: "Đe dọa bạo lực đáng tin cậy", summary: "Áp dụng cho tuyên bố đáng tin cậy về ý định gây tổn hại thể chất cho người, nhóm hoặc địa điểm." },
  },
  "CSR.SAF.002": {
    en: { title: "Violence or exploitation", summary: "Covers content that promotes, glorifies, or facilitates violence or exploitation." },
    vi: { title: "Bạo lực hoặc bóc lột", summary: "Áp dụng cho nội dung cổ súy, tôn vinh hoặc tạo điều kiện cho bạo lực hay bóc lột." },
  },
  "CSR.SAF.003": {
    en: { title: "Self-harm risk", summary: "Covers content encouraging or instructing self-harm or dangerous disordered-eating behavior." },
    vi: { title: "Nguy cơ tự gây hại", summary: "Áp dụng cho nội dung khuyến khích hoặc hướng dẫn tự gây hại hay hành vi ăn uống rối loạn nguy hiểm." },
  },
  "CSR.HATE.001": {
    en: { title: "Hateful conduct", summary: "Covers attacks or dehumanizing claims based on protected characteristics." },
    vi: { title: "Hành vi thù ghét", summary: "Áp dụng cho nội dung công kích hoặc phi nhân hóa dựa trên đặc điểm được bảo vệ." },
  },
  "CSR.INT.001": {
    en: { title: "Impersonation", summary: "Covers deceptive presentation as another person, organization, or authoritative identity." },
    vi: { title: "Mạo danh", summary: "Áp dụng cho việc gian dối tự nhận là cá nhân, tổ chức hoặc danh tính có thẩm quyền khác." },
  },
  "CSR.INT.002": {
    en: { title: "False or misleading information", summary: "Covers materially false claims when authoritative facts and relevant context are available." },
    vi: { title: "Thông tin sai lệch hoặc gây hiểu nhầm", summary: "Áp dụng cho tuyên bố sai lệch đáng kể khi có dữ kiện có thẩm quyền và ngữ cảnh liên quan." },
  },
  "CSR.INT.003": {
    en: { title: "Scam, fraud, or phishing", summary: "Covers phishing links, sensitive credential requests, money-transfer demands, guaranteed-profit claims, and fake rewards or vouchers." },
    vi: { title: "Lừa đảo, gian lận hoặc phishing", summary: "Áp dụng cho liên kết phishing, yêu cầu OTP hoặc mật khẩu, yêu cầu chuyển tiền, cam kết lợi nhuận và phần thưởng hoặc voucher giả." },
  },
  "CSR.SEX.001": {
    en: { title: "Nudity or sexual activity", summary: "Covers explicit nudity or sexual activity subject to age, consent, and contextual exceptions." },
    vi: { title: "Khỏa thân hoặc hoạt động tình dục", summary: "Áp dụng cho nội dung khỏa thân hoặc hoạt động tình dục rõ ràng, có xét tuổi, sự đồng thuận và ngoại lệ ngữ cảnh." },
  },
  "CSR.SEX.002": {
    en: { title: "Sexual solicitation", summary: "Covers explicit sexual requests, services, or exploitative sexual solicitation." },
    vi: { title: "Gạ gẫm tình dục", summary: "Áp dụng cho yêu cầu, dịch vụ hoặc lời gạ gẫm tình dục rõ ràng và mang tính bóc lột." },
  },
  "CSR.PRIV.001": {
    en: { title: "Privacy violation", summary: "Covers unauthorized disclosure or solicitation of personal, private, or sensitive information." },
    vi: { title: "Vi phạm quyền riêng tư", summary: "Áp dụng cho việc tiết lộ hoặc yêu cầu thông tin cá nhân, riêng tư hay nhạy cảm khi chưa được phép." },
  },
  "CSR.IP.001": {
    en: { title: "Copyright violation", summary: "Covers unauthorized copying or distribution when claimant authority and licensing context are available." },
    vi: { title: "Vi phạm bản quyền", summary: "Áp dụng cho việc sao chép hoặc phân phối trái phép khi có căn cứ về quyền khiếu nại và giấy phép." },
  },
  "CSR.IP.002": {
    en: { title: "Other intellectual-property violation", summary: "Covers trademark or other intellectual-property claims requiring authority and permission evidence." },
    vi: { title: "Vi phạm sở hữu trí tuệ khác", summary: "Áp dụng cho khiếu nại nhãn hiệu hoặc sở hữu trí tuệ khác cần bằng chứng về thẩm quyền và sự cho phép." },
  },
  "CSR.COM.001": {
    en: { title: "Restricted goods or services", summary: "Covers offers or transactions involving goods or services restricted by applicable policy or law." },
    vi: { title: "Hàng hóa hoặc dịch vụ bị hạn chế", summary: "Áp dụng cho lời chào bán hoặc giao dịch hàng hóa, dịch vụ bị chính sách hay pháp luật liên quan hạn chế." },
  },
  "CSR.REL.001": {
    en: { title: "Off-topic or irrelevant content", summary: "Covers content materially unrelated to the conversation or CafeStory context under review." },
    vi: { title: "Nội dung lạc đề hoặc không liên quan", summary: "Áp dụng cho nội dung không liên quan đáng kể đến cuộc trò chuyện hoặc ngữ cảnh CafeStory đang được xem xét." },
  },
};

export function getAdminReportAiPolicyCopy(ruleId: string, locale: Locale) {
  return policyCopy[ruleId as AdminReportAiRuleId]?.[locale] ?? null;
}
