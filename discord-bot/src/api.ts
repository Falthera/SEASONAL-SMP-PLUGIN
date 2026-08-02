import { ApiResponse } from './types';

export class WhitelistAPIClient {
  private baseUrl: string;
  private apiKey: string;

  constructor(baseUrl: string, apiKey: string) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
  }

  async addPlayer(discordId: string, username: string): Promise<ApiResponse> {
    const payload = { discordId, username };
    return this.post('/api/whitelist/add', JSON.stringify(payload));
  }

  async removePlayer(uuid: string): Promise<ApiResponse> {
    const payload = { uuid };
    return this.post('/api/whitelist/remove', JSON.stringify(payload));
  }

  async lookupByUuid(uuid: string): Promise<ApiResponse> {
    return this.get(`/api/whitelist/lookup?uuid=${encodeURIComponent(uuid)}`);
  }

  async lookupByUsername(username: string): Promise<ApiResponse> {
    return this.get(`/api/whitelist/lookup?username=${encodeURIComponent(username)}`);
  }

  async getStats(): Promise<ApiResponse> {
    return this.get('/api/whitelist/stats');
  }

  async healthCheck(): Promise<ApiResponse> {
    return this.post('/api/health', '');
  }

  async reload(): Promise<ApiResponse> {
    return this.post('/api/reload', '');
  }

  private async post(path: string, body: string): Promise<ApiResponse> {
    try {
      const response = await fetch(this.baseUrl + path, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`,
        },
        body: body || undefined,
      });

      const responseBody = await response.text();
      return new ApiResponse(response.status, responseBody);
    } catch (error: any) {
      return new ApiResponse(0, JSON.stringify({ error: `Failed to connect to whitelist API: ${error.message}` }));
    }
  }

  private async get(path: string): Promise<ApiResponse> {
    try {
      const response = await fetch(this.baseUrl + path, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${this.apiKey}`,
        },
      });

      const responseBody = await response.text();
      return new ApiResponse(response.status, responseBody);
    } catch (error: any) {
      return new ApiResponse(0, JSON.stringify({ error: `Failed to connect to whitelist API: ${error.message}` }));
    }
  }
}
