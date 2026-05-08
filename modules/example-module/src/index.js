/**
 * Example Module — Dummy Implementation
 *
 * Demonstrates the Module Slot Contract.
 * Echoes input text back unchanged.
 */

export function process(input) {
  if (typeof input !== "string") {
    return {
      success: false,
      error: "Expected text/plain input.",
      output: null
    };
  }

  return {
    success: true,
    error: null,
    output: input
  };
}

export const meta = {
  name: "Example Module",
  version: "0.1.0"
};
