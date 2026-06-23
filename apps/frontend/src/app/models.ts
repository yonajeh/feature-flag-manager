export interface Application {
  id: string;
  name: string;
  displayName: string;
  createdAt: string;
  updatedAt: string;
}

export interface FeatureFlag {
  id: string;
  applicationId: string;
  key: string;
  enabled: boolean;
  description?: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export interface ApiTokenMetadata {
  id: string;
  applicationId: string;
  tokenPrefix: string;
  status: 'ACTIVE' | 'REVOKED';
  lastUsedAt?: string;
  expiresAt?: string;
  createdAt: string;
  revokedAt?: string;
}

export interface ApiTokenCreated extends ApiTokenMetadata {
  plaintextToken: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
}

export interface ExportedApplication {
  application: Application;
  featureFlags: FeatureFlag[];
  tokens: ApiTokenMetadata[];
}

export interface ApplicationDataExport {
  exportedAt: string;
  version: number;
  application: Application;
  featureFlags: FeatureFlag[];
  tokens: ApiTokenMetadata[];
}

export interface FullDataExport {
  exportedAt: string;
  version: number;
  applications: ExportedApplication[];
}
