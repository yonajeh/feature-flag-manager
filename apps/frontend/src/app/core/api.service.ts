import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  Application,
  ApiTokenCreated,
  ApiTokenMetadata,
  ApplicationDataExport,
  FeatureFlag,
  FullDataExport,
} from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  listApplications() {
    return this.http.get<Application[]>('/api/admin/applications');
  }

  createApplication(name: string, displayName: string) {
    return this.http.post<Application>('/api/admin/applications', { name, displayName });
  }

  deleteApplication(id: string) {
    return this.http.delete<void>(`/api/admin/applications/${id}`);
  }

  listFlags(applicationId: string) {
    return this.http.get<FeatureFlag[]>(`/api/admin/applications/${applicationId}/flags`);
  }

  createFlag(applicationId: string, key: string, enabled: boolean, description?: string) {
    return this.http.post<FeatureFlag>(`/api/admin/applications/${applicationId}/flags`, {
      key,
      enabled,
      description,
    });
  }

  updateFlag(applicationId: string, key: string, enabled: boolean, description?: string) {
    return this.http.put<FeatureFlag>(`/api/admin/applications/${applicationId}/flags/${key}`, {
      enabled,
      description,
    });
  }

  deleteFlag(applicationId: string, key: string) {
    return this.http.delete<void>(`/api/admin/applications/${applicationId}/flags/${key}`);
  }

  listTokens(applicationId: string) {
    return this.http.get<ApiTokenMetadata[]>(`/api/admin/applications/${applicationId}/tokens`);
  }

  generateToken(applicationId: string) {
    return this.http.post<ApiTokenCreated>(`/api/admin/applications/${applicationId}/tokens`, {});
  }

  rotateToken(applicationId: string, tokenId: string) {
    return this.http.post<ApiTokenCreated>(
      `/api/admin/applications/${applicationId}/tokens/${tokenId}/rotate`,
      {}
    );
  }

  revokeToken(applicationId: string, tokenId: string) {
    return this.http.post<ApiTokenMetadata>(
      `/api/admin/applications/${applicationId}/tokens/${tokenId}/revoke`,
      {}
    );
  }

  exportApplication(applicationId: string) {
    return this.http.get<ApplicationDataExport>(`/api/admin/applications/${applicationId}/export`);
  }

  exportAll() {
    return this.http.get<FullDataExport>('/api/admin/export');
  }
}
