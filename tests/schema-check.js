/**
 * ANVIL Gate 001 — Schema Structure Check
 *
 * Validates that fixture objects satisfy the required fields of each core schema.
 * No external dependencies. Run with: node tests/schema-check.js
 */

'use strict';

const path = require('path');
const fs = require('fs');

// --- Fixtures (minimal valid objects for each schema) ---

const fixtures = {
  provider: {
    id: 'openai',
    name: 'OpenAI',
    endpoint: 'https://api.openai.com/v1',
    auth_type: 'bearer',
    models: ['gpt-4o', 'gpt-4o-mini'],
    status: 'active',
    fallback_to: null
  },

  'agent-run': {
    run_id: 'RUN_20260512_143000_001',
    started_at: '2026-05-12T14:30:00Z',
    finished_at: null,
    agent: 'claude-code',
    tool: 'claude-code-cli',
    input: 'Establish the ANVIL core skeleton',
    goal: 'Create all Gate 001 files with no execution code',
    status: 'stable',
    changed_files: [
      {
        path: 'docs/ANVIL_CORE_MANIFEST.md',
        action: 'create',
        diff: null,
        timestamp: '2026-05-12T14:30:00Z',
        rollback_available: true
      }
    ],
    tests_run: [
      {
        test_name: 'schema-check',
        status: 'pass',
        output: 'All 6 schema checks passed',
        timestamp: '2026-05-12T14:35:00Z'
      }
    ],
    failure_state: null,
    recovery_step: null,
    artifacts: ['OUT_20260512_143000_001'],
    human_review_required: true,
    gate: '001'
  },

  gate: {
    gate_id: '001',
    name: 'Core Skeleton',
    status: 'done',
    branch: 'claude/anvil-core-skeleton-F4YXT',
    acceptance: [
      { criterion: 'docs/ANVIL_CORE_MANIFEST.md exists', met: true, evidence: 'file present' }
    ],
    kill: [
      'Execution code added to any src/core/ subdirectory',
      'UI (app/) modified'
    ],
    files: ['docs/ANVIL_CORE_MANIFEST.md', 'gates/GATE_001_CORE_SKELETON.md'],
    not_built: ['OPENDORK runtime', 'CATALON orchestration engine'],
    open_conflicts: [
      {
        conflict: 'src/core/providers/ vs src/core/opendork/',
        resolution: 'opendork/ is named entry point, providers/ is implementation home'
      }
    ],
    started_at: '2026-05-12T14:00:00Z',
    completed_at: '2026-05-12T15:00:00Z'
  },

  artifact: {
    output_id: 'OUT_20260512_143000_001',
    type: 'text/markdown',
    lifecycle: 'final',
    origin: {
      module: 'prompt-pack-builder',
      workspace: 'ANVIL Core',
      run_id: 'RUN_20260512_143000_001'
    },
    filename: 'ANVIL_CORE_MANIFEST.md',
    created_at: '2026-05-12T14:30:00Z',
    size_bytes: 2048,
    checksum_sha256: null,
    gate: '001',
    discarded_at: null,
    discarded_reason: null
  },

  project: {
    id: 'catchit',
    name: 'CatchIt',
    repo: null,
    status: 'active',
    gate_phase: 'Planning',
    notes: 'State-surface UX + mobility trust tool',
    removed_at: null,
    removed_reason: null
  },

  'module-contract': {
    name: 'prompt-pack-builder',
    purpose: 'Builds structured agent handoff prompt packs from workspace context.',
    inputs: ['text/plain', 'config/json'],
    outputs: ['text/markdown', 'application/json'],
    requiredPermissions: ['filesystem.read'],
    canRunOffline: true,
    canExport: true,
    failureBehavior: 'Returns an empty prompt pack with an error message in the header.',
    status: 'stable',
    version: '1.0.0'
  }
};

// --- Schema required-field definitions ---

const requiredFields = {
  provider: ['id', 'name', 'endpoint', 'auth_type', 'models', 'status'],
  'agent-run': ['run_id', 'started_at', 'agent', 'tool', 'input', 'goal', 'status', 'changed_files', 'tests_run'],
  gate: ['gate_id', 'name', 'status', 'acceptance', 'kill', 'files'],
  artifact: ['output_id', 'type', 'lifecycle', 'origin', 'filename', 'created_at'],
  project: ['id', 'name', 'status', 'gate_phase'],
  'module-contract': ['name', 'purpose', 'inputs', 'outputs', 'requiredPermissions', 'canRunOffline', 'canExport', 'failureBehavior']
};

const enumChecks = {
  provider: { auth_type: ['bearer', 'api-key', 'oauth'], status: ['active', 'degraded', 'disabled'] },
  'agent-run': { status: ['stable', 'adapting', 'act_now', 'failed'] },
  gate: { status: ['pending', 'in_progress', 'done', 'blocked', 'failed', 'prototype', 'partial', 'docs-only'] },
  artifact: { lifecycle: ['raw', 'final', 'broken', 'discarded'] },
  project: { status: ['active', 'paused', 'archived', 'planned'] },
  'module-contract': {}
};

// --- Validator ---

function checkSchema(name, fixture) {
  const errors = [];
  const required = requiredFields[name] || [];
  const enums = enumChecks[name] || {};

  for (const field of required) {
    if (fixture[field] === undefined) {
      errors.push(`missing required field: "${field}"`);
    }
  }

  for (const [field, allowed] of Object.entries(enums)) {
    if (fixture[field] !== undefined && !allowed.includes(fixture[field])) {
      errors.push(`invalid enum value for "${field}": "${fixture[field]}" (allowed: ${allowed.join(', ')})`);
    }
  }

  return errors;
}

function checkSchemaFileExists(name) {
  const schemaPath = path.join(__dirname, '..', 'src', 'core', 'types', `${name}.schema.json`);
  try {
    const raw = fs.readFileSync(schemaPath, 'utf8');
    JSON.parse(raw);
    return null;
  } catch (e) {
    return `schema file missing or invalid JSON: ${schemaPath}`;
  }
}

// --- Run checks ---

const schemaNames = Object.keys(fixtures);
const results = [];
let allPassed = true;

for (const name of schemaNames) {
  const fileError = checkSchemaFileExists(name);
  const fixtureErrors = checkSchema(name, fixtures[name]);
  const errors = [fileError, ...fixtureErrors].filter(Boolean);
  const passed = errors.length === 0;
  if (!passed) allPassed = false;
  results.push({ name, passed, errors });
}

// --- Output ---

console.log('\nANVIL Gate 001 — Schema Structure Check');
console.log('='.repeat(50));

for (const { name, passed, errors } of results) {
  const mark = passed ? 'PASS' : 'FAIL';
  console.log(`\n[${mark}] ${name}.schema.json`);
  if (!passed) {
    for (const err of errors) {
      console.log(`       - ${err}`);
    }
  }
}

console.log('\n' + '='.repeat(50));
if (allPassed) {
  console.log('All schema checks passed.\n');
  process.exit(0);
} else {
  console.log('One or more schema checks FAILED.\n');
  process.exit(1);
}
