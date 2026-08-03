import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class G006CDbAudit {
    private record Query(String name, String sql) {
    }

    private static final Path ENV_FILE = Path.of(
            "1-cafe-story-backend-javaspring",
            "src",
            "main",
            "resources",
            ".env");

    private static final List<Query> QUERIES = List.of(
            new Query("DB_SESSION", """
                    select current_schema() as schema_name,
                           current_setting('transaction_read_only') as transaction_read_only,
                           current_setting('server_version') as server_version
                    """),
            new Query("TABLE_EXISTENCE", """
                    select table_name
                    from information_schema.tables
                    where table_schema = current_schema()
                      and table_name in (
                        'report_reasons',
                        'content_reports',
                        'ai_moderation_results',
                        'admin_report_ai_resolutions',
                        'admin_report_ai_auto_apply_jobs',
                        'flyway_schema_history'
                      )
                    order by table_name
                    """),
            new Query("RELEVANT_COLUMNS", """
                    select table_name,
                           ordinal_position,
                           column_name,
                           data_type,
                           is_nullable
                    from information_schema.columns
                    where table_schema = current_schema()
                      and table_name in (
                        'report_reasons',
                        'content_reports',
                        'admin_report_ai_resolutions',
                        'admin_report_ai_auto_apply_jobs'
                      )
                    order by table_name, ordinal_position
                    """),
            new Query("RELEVANT_CONSTRAINTS", """
                    select c.conrelid::regclass::text as table_name,
                           c.conname as constraint_name,
                           case c.contype
                             when 'p' then 'PRIMARY_KEY'
                             when 'f' then 'FOREIGN_KEY'
                             when 'u' then 'UNIQUE'
                             when 'c' then 'CHECK'
                             else c.contype::text
                           end as constraint_type,
                           pg_get_constraintdef(c.oid) as definition
                    from pg_constraint c
                    where c.conrelid in (
                      'report_reasons'::regclass,
                      'content_reports'::regclass,
                      'admin_report_ai_resolutions'::regclass,
                      'admin_report_ai_auto_apply_jobs'::regclass
                    )
                    order by table_name, constraint_name
                    """),
            new Query("RELEVANT_INDEXES", """
                    select tablename, indexname, indexdef
                    from pg_indexes
                    where schemaname = current_schema()
                      and tablename in (
                        'report_reasons',
                        'content_reports',
                        'admin_report_ai_resolutions',
                        'admin_report_ai_auto_apply_jobs'
                      )
                    order by tablename, indexname
                    """),
            new Query("FLYWAY_AI_MIGRATIONS", """
                    select version, description, type, success
                    from flyway_schema_history
                    where version in ('20260705.01', '20260706.01')
                    order by installed_rank
                    """),
            new Query("REPORT_REASON_CATALOG", """
                    select code,
                           label_vi,
                           coalesce(target_type::text, 'ALL') as target_type,
                           severity,
                           requires_description,
                           is_active,
                           sort_order
                    from report_reasons
                    order by sort_order, code
                    """),
            new Query("REPORT_REASON_SUMMARY", """
                    select count(*) as total_reasons,
                           count(*) filter (where is_active) as active_reasons,
                           count(*) filter (where not is_active) as inactive_reasons,
                           min(severity) as min_severity,
                           max(severity) as max_severity,
                           count(*) filter (where requires_description) as requires_description_count
                    from report_reasons
                    """),
            new Query("REPORT_STATUS_TARGET_DISTRIBUTION", """
                    select status::text as status,
                           target_type::text as target_type,
                           count(*) as report_count
                    from content_reports
                    group by status, target_type
                    order by status, target_type
                    """),
            new Query("REPORT_REASON_USAGE", """
                    select coalesce(rr.code, '<NULL_OR_MISSING>') as reason_code,
                           count(*) as report_count
                    from content_reports cr
                    left join report_reasons rr on rr.id = cr.reason_id
                    group by coalesce(rr.code, '<NULL_OR_MISSING>')
                    order by report_count desc, reason_code
                    """),
            new Query("REPORT_DATA_QUALITY", """
                    select count(*) as total_reports,
                           count(*) filter (where reason_id is null) as null_reason_id,
                           count(*) filter (where reason is null or btrim(reason) = '') as blank_reason_snapshot,
                           count(*) filter (where num_nonnulls(blog_id, comment_id, reported_user_id, cafe_page_id) <> 1)
                             as invalid_target_cardinality,
                           count(*) filter (
                             where (target_type::text = 'BLOG' and blog_id is null)
                                or (target_type::text = 'COMMENT' and comment_id is null)
                                or (target_type::text = 'USER' and reported_user_id is null)
                                or (target_type::text = 'CAFE_PAGE' and cafe_page_id is null)
                           ) as target_type_link_mismatch,
                           count(*) filter (where length(description) > 2000) as description_over_2000
                    from content_reports
                    """),
            new Query("REPORT_REASON_REFERENCE_QUALITY", """
                    select count(*) filter (where rr.id is null) as missing_reason_reference,
                           count(*) filter (where rr.id is not null and not rr.is_active) as inactive_reason_reference,
                           count(*) filter (
                             where rr.id is not null
                               and rr.target_type is not null
                               and rr.target_type::text <> cr.target_type::text
                           ) as reason_target_mismatch
                    from content_reports cr
                    left join report_reasons rr on rr.id = cr.reason_id
                    """),
            new Query("AI_RESOLUTION_SUMMARY", """
                    select count(*) as total_resolutions,
                           count(*) filter (where confidence_score is null) as null_confidence,
                           count(*) filter (where risk_score is null) as null_risk,
                           count(*) filter (where explanation is null or btrim(explanation) = '') as blank_explanation,
                           count(*) filter (where model_name is null or btrim(model_name) = '') as blank_model,
                           count(*) filter (where raw_response is null) as null_raw_response,
                           count(*) filter (where labels is null) as null_labels
                    from admin_report_ai_resolutions
                    """),
            new Query("AI_RESOLUTION_DECISION_ACTION", """
                    select report_decision,
                           target_type,
                           target_action,
                           count(*) as resolution_count
                    from admin_report_ai_resolutions
                    group by report_decision, target_type, target_action
                    order by report_decision, target_type, target_action
                    """),
            new Query("AI_RESOLUTION_SCORE_BY_DECISION_ACTION", """
                    select report_decision,
                           target_type,
                           target_action,
                           count(*) as resolution_count,
                           min(confidence_score) as min_confidence,
                           max(confidence_score) as max_confidence,
                           round(avg(confidence_score)::numeric, 2) as avg_confidence,
                           min(risk_score) as min_risk,
                           max(risk_score) as max_risk,
                           round(avg(risk_score)::numeric, 2) as avg_risk
                    from admin_report_ai_resolutions
                    group by report_decision, target_type, target_action
                    order by report_decision, target_type, target_action
                    """),
            new Query("AI_RESOLUTION_LEGACY_THRESHOLD_PROFILE", """
                    select count(*) filter (
                             where report_decision = 'RESOLVE' and risk_score < 70
                           ) as resolve_below_legacy_auto_threshold,
                           count(*) filter (
                             where report_decision = 'RESOLVE' and confidence_score < 80
                           ) as resolve_below_legacy_confidence_threshold,
                           count(*) filter (
                             where report_decision = 'NEEDS_MANUAL_REVIEW' and confidence_score >= 80
                           ) as manual_review_high_confidence
                    from admin_report_ai_resolutions
                    """),
            new Query("AI_RESOLUTION_SCORE_PROFILE", """
                    select min(confidence_score) as min_confidence,
                           max(confidence_score) as max_confidence,
                           round(avg(confidence_score)::numeric, 2) as avg_confidence,
                           min(risk_score) as min_risk,
                           max(risk_score) as max_risk,
                           round(avg(risk_score)::numeric, 2) as avg_risk,
                           count(*) filter (
                             where confidence_score < 0
                                or confidence_score > 100
                                or confidence_score::text in ('NaN', 'Infinity', '-Infinity')
                           ) as invalid_confidence,
                           count(*) filter (
                             where risk_score < 0
                                or risk_score > 100
                                or risk_score::text in ('NaN', 'Infinity', '-Infinity')
                           ) as invalid_risk
                    from admin_report_ai_resolutions
                    """),
            new Query("AI_RESOLUTION_INVALID_COMBINATIONS", """
                    select count(*) as invalid_combination_count
                    from admin_report_ai_resolutions
                    where not (
                      (report_decision = 'NEEDS_MANUAL_REVIEW' and target_action = 'NONE')
                      or (report_decision = 'REJECT'
                          and ((target_type in ('BLOG', 'COMMENT') and target_action = 'APPROVE')
                            or (target_type in ('USER', 'CAFE_PAGE') and target_action = 'KEEP_ACTIVE')))
                      or (report_decision = 'RESOLVE'
                          and ((target_type in ('BLOG', 'COMMENT') and target_action in ('HIDE', 'REMOVE'))
                            or (target_type = 'USER' and target_action = 'SUSPEND_USER')
                            or (target_type = 'CAFE_PAGE' and target_action = 'SUSPEND_PAGE')))
                    )
                    """),
            new Query("AI_RESOLUTION_MODELS", """
                    select coalesce(model_name, '<NULL>') as model_name,
                           count(*) as resolution_count
                    from admin_report_ai_resolutions
                    group by coalesce(model_name, '<NULL>')
                    order by resolution_count desc, model_name
                    """),
            new Query("AI_RESOLUTION_RULES_TOP20", """
                    select coalesce(rule_code, '<NULL>') as rule_code,
                           count(*) as resolution_count
                    from admin_report_ai_resolutions
                    group by coalesce(rule_code, '<NULL>')
                    order by resolution_count desc, rule_code
                    limit 20
                    """),
            new Query("AUTO_JOB_SUMMARY", """
                    select count(*) as total_jobs,
                           count(*) filter (where status in ('SCHEDULED', 'APPLYING')) as active_jobs,
                           count(*) filter (where status = 'APPLIED') as applied_jobs,
                           count(*) filter (where status = 'CANCELLED') as cancelled_jobs,
                           count(*) filter (where status = 'SKIPPED') as skipped_jobs,
                           count(*) filter (where status = 'FAILED') as failed_jobs
                    from admin_report_ai_auto_apply_jobs
                    """),
            new Query("AUTO_JOB_STATUS_ACTION", """
                    select status,
                           report_decision,
                           target_type,
                           target_action,
                           count(*) as job_count
                    from admin_report_ai_auto_apply_jobs
                    group by status, report_decision, target_type, target_action
                    order by status, report_decision, target_type, target_action
                    """),
            new Query("AUTO_JOB_DESTRUCTIVE_ACTIONS", """
                    select target_action,
                           status,
                           count(*) as job_count
                    from admin_report_ai_auto_apply_jobs
                    where target_action in ('REMOVE', 'SUSPEND_USER', 'SUSPEND_PAGE')
                    group by target_action, status
                    order by target_action, status
                    """),
            new Query("AUTO_JOB_DATA_QUALITY", """
                    select count(*) filter (where status = 'APPLIED' and applied_at is null)
                             as applied_without_timestamp,
                           count(*) filter (where status = 'CANCELLED' and cancelled_at is null)
                             as cancelled_without_timestamp,
                           count(*) filter (where status in ('SCHEDULED', 'APPLYING') and scheduled_at is null)
                             as active_without_schedule,
                           count(*) filter (where confidence_score is null) as null_confidence,
                           count(*) filter (where risk_score is null) as null_risk,
                           count(*) filter (
                             where confidence_score < 0
                                or confidence_score > 100
                                or confidence_score::text in ('NaN', 'Infinity', '-Infinity')
                           ) as invalid_confidence,
                           count(*) filter (
                             where risk_score < 0
                                or risk_score > 100
                                or risk_score::text in ('NaN', 'Infinity', '-Infinity')
                           ) as invalid_risk
                    from admin_report_ai_auto_apply_jobs
                    """),
            new Query("AUTO_JOB_ACTIVE_DUPLICATES", """
                    select count(*) as report_with_multiple_active_jobs
                    from (
                      select content_report_id
                      from admin_report_ai_auto_apply_jobs
                      where status in ('SCHEDULED', 'APPLYING')
                      group by content_report_id
                      having count(*) > 1
                    ) duplicated
                    """)
    );

    private G006CDbAudit() {
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> env = readDotEnv(ENV_FILE);
        String url = required(env, "DB_URL");
        String username = required(env, "DB_USERNAME");
        String dbCredential = required(env, "DB_PASSWORD");

        Class.forName("org.postgresql.Driver");
        try (Connection connection = DriverManager.getConnection(url, username, dbCredential)) {
            connection.setReadOnly(true);
            connection.setAutoCommit(false);
            try (Statement guard = connection.createStatement()) {
                guard.execute("set transaction read only");
                guard.execute("set local statement_timeout = '15000ms'");
                guard.execute("set local lock_timeout = '3000ms'");
            }

            for (Query query : QUERIES) {
                runQuery(connection, query);
            }
            connection.rollback();
            System.out.println("AUDIT_END\tROLLBACK_COMPLETE");
        } catch (SQLException exception) {
            System.out.println("AUDIT_CONNECTION_ERROR\tclass="
                    + exception.getClass().getSimpleName()
                    + "\tsqlState="
                    + safe(exception.getSQLState()));
            throw exception;
        }
    }

    private static void runQuery(Connection connection, Query query) {
        System.out.println("SECTION\t" + query.name());
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query.sql())) {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            List<String> headers = new ArrayList<>();
            for (int index = 1; index <= columnCount; index++) {
                headers.add(metadata.getColumnLabel(index));
            }
            System.out.println(String.join("\t", headers));

            int rowCount = 0;
            while (resultSet.next()) {
                List<String> values = new ArrayList<>();
                for (int index = 1; index <= columnCount; index++) {
                    values.add(safe(resultSet.getString(index)));
                }
                System.out.println(String.join("\t", values));
                rowCount++;
            }
            System.out.println("ROWS\t" + rowCount);
        } catch (SQLException exception) {
            System.out.println("QUERY_ERROR\tsqlState=" + safe(exception.getSQLState())
                    + "\tclass=" + exception.getClass().getSimpleName());
        }
    }

    private static Map<String, String> readDotEnv(Path path) throws Exception {
        Map<String, String> values = new LinkedHashMap<>();
        for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = rawLine.trim();
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("export ")) {
                line = line.substring("export ".length()).trim();
            }
            int equals = line.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();
            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }
            values.put(key, value);
        }
        return values;
    }

    private static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required database configuration key: " + key);
        }
        return value;
    }

    private static String safe(String value) {
        if (value == null) {
            return "<NULL>";
        }
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '\t' || character == '\r' || character == '\n') {
                escaped.append(' ');
            } else if (character < 32 || character > 126) {
                escaped.append(String.format("\\u%04X", (int) character));
            } else {
                escaped.append(character);
            }
        }
        return escaped.toString();
    }
}
