import type { ProviderSetup } from './knownProviders'

export function generateConfigJson(
  providers: ProviderSetup[],
  host: string,
  port: number,
  gatewayKeyEnvVar: string,
): string {
  const config = {
    host,
    port,
    gatewayKeyEnv: gatewayKeyEnvVar,
    providers: providers.map(p => ({
      id: p.id,
      baseUrl: p.baseUrl,
      ...(p.local ? {} : { apiKeyEnv: p.apiKeyEnvVar }),
      ...(p.models.length > 0 ? { models: p.models } : {}),
      local: p.local,
      ...(Object.keys(p.headers).length > 0 ? { headers: p.headers } : {}),
    })),
  }
  return JSON.stringify(config, null, 2)
}

export function generateStartBat(
  providers: ProviderSetup[],
  keyValues: Record<string, string>,
  gatewayKey: string,
  gatewayKeyEnvVar: string,
): string {
  const lines: string[] = [
    '@echo off',
    'setlocal',
    '',
    `set ${gatewayKeyEnvVar}=${gatewayKey}`,
  ]
  providers
    .filter(p => !p.local)
    .forEach(p => lines.push(`set ${p.apiKeyEnvVar}=${keyValues[p.id] ?? ''}`))
  lines.push('', 'bellows serve --config "%~dp0bellows.config.json"', 'pause')
  return lines.join('\r\n')
}

export function generateStartSh(
  providers: ProviderSetup[],
  keyValues: Record<string, string>,
  gatewayKey: string,
  gatewayKeyEnvVar: string,
): string {
  const lines: string[] = [
    '#!/bin/bash',
    '',
    `export ${gatewayKeyEnvVar}="${gatewayKey}"`,
  ]
  providers
    .filter(p => !p.local)
    .forEach(p => lines.push(`export ${p.apiKeyEnvVar}="${keyValues[p.id] ?? ''}"`))
  lines.push('', 'exec bellows serve --config "$(dirname "$0")/bellows.config.json"')
  return lines.join('\n')
}

export function downloadFile(filename: string, content: string): void {
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
