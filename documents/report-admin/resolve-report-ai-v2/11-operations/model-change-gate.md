# Model and Prompt Change Gate

Any model, prompt, schema or workflow change requires:

1. new immutable version;
2. impact/risk review;
3. contract and adversarial dataset pass;
4. comparison by target/rule/evidence slice;
5. privacy/cost/latency review;
6. canary and rollback plan;
7. named approval.

Changing an environment alias without persisting the resolved model identity is prohibited.
Fallback to another model is not automatic unless the fallback version has passed the same gate.

No model change may alter A0 or Backend semantic authority.
