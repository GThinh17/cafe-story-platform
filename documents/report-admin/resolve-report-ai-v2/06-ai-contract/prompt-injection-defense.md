# Prompt Injection Defense

Controls:

- system/data message separation;
- serialized JSON data, no string-concatenated instructions;
- rule/action allowlist from Backend;
- strict no-tool execution;
- evidence-reference validation;
- length/count limits and sanitization;
- adversarial fixtures;
- Backend ignores model attempts to modify versions/constraints.

Injection signals may be logged as codes, not raw content. A detected injection does not prove a
content-policy violation; it triggers manual/operational handling as appropriate.
