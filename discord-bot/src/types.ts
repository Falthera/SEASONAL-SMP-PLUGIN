export class ApiResponse {
  statusCode: number;
  body: string;

  constructor(statusCode: number, body: string) {
    this.statusCode = statusCode;
    this.body = body;
  }

  isSuccess(): boolean {
    return this.statusCode >= 200 && this.statusCode < 300;
  }

  getMessage(): string {
    try {
      const parsed = JSON.parse(this.body);
      return parsed.error || parsed.message || this.body;
    } catch {
      return this.body;
    }
  }

  getUsername(): string | null {
    try {
      const parsed = JSON.parse(this.body);
      return parsed.username ?? null;
    } catch {
      return null;
    }
  }

  getUuid(): string | null {
    try {
      const parsed = JSON.parse(this.body);
      return parsed.uuid ?? null;
    } catch {
      return null;
    }
  }
}
