/* ═══════════════════════════════════════════
   Prompt Pack Builder — Module
   Gate A12: First Real Module
   ═══════════════════════════════════════════ */

/**
 * Build a structured prompt pack from inputs.
 * No LLM needed. No external API needed.
 *
 * @param {Object} opts
 * @param {string} opts.project - Projektname
 * @param {string} opts.goal - Ziel
 * @param {string[]} opts.constraints - Harte Constraints
 * @param {string} opts.nextGate - Nächste Gate
 * @param {string} opts.agentTarget - Zielsystem (viktor/claude/codex)
 * @param {string[]} [opts.contextFiles] - Kontext-Dateien
 * @param {string[]} [opts.killCriteria] - Kill-Kriterien
 * @param {string} [opts.currentState] - Ist-Zustand
 * @param {string} [opts.dod] - Definition of Done
 * @returns {{ markdown: string, manifest: Object }}
 */
function buildPromptPack(opts) {
  // Validate required fields
  const required = ["project", "goal", "constraints", "nextGate", "agentTarget"];
  for (const field of required) {
    if (!opts[field]) {
      throw new Error(`Pflichtfeld fehlt: ${field}`);
    }
  }

  const constraints = opts.constraints || [];
  const contextFiles = opts.contextFiles || [];
  const killCriteria = opts.killCriteria || ["Scope Creep → sofort stoppen"];
  const currentState = opts.currentState || "Nicht angegeben.";
  const dod = opts.dod || "Alle Aufgaben der Gate erledigt.";
  const now = new Date().toISOString();

  // Build Markdown
  const md = [
    `# Agent Prompt Pack: ${opts.project}`,
    "",
    `> Erstellt: ${now}`,
    `> Agent: ${opts.agentTarget}`,
    "",
    "## Ziel",
    "",
    opts.goal,
    "",
    "## Ist-Zustand",
    "",
    currentState,
    "",
    "## Nächste Gate",
    "",
    opts.nextGate,
    "",
    "## Harte Constraints",
    "",
    ...constraints.map((c) => `- ${c}`),
    "",
    "## Definition of Done",
    "",
    dod,
    "",
    "## Kill-Kriterien",
    "",
    ...killCriteria.map((k) => `- ${k}`),
    "",
    "## Agent-Zielsystem",
    "",
    opts.agentTarget,
    "",
  ];

  if (contextFiles.length > 0) {
    md.push("## Kontext-Dateien", "");
    for (const f of contextFiles) {
      md.push("- `" + f + "`");
    }
    md.push("");
  }

  // Build manifest
  const manifest = {
    pack_type: "agent-handoff",
    version: 1,
    project: opts.project,
    agent_target: opts.agentTarget,
    created_at: now,
    goal: opts.goal,
    next_gate: opts.nextGate,
    constraints,
    kill_criteria: killCriteria,
    context_files: contextFiles,
  };

  return {
    markdown: md.join("\n"),
    manifest,
  };
}

// Export for Node.js / module systems
if (typeof module !== "undefined" && module.exports) {
  module.exports = { buildPromptPack };
}
