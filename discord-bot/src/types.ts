export interface ApiResponse {
  statusCode: number;
  body: string;
  isSuccess(): boolean;
  getMessage(): string;
  getUsername(): string | null;
  getUuid(): string | null;
}
