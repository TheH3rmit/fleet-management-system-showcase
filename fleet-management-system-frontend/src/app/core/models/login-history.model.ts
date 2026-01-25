export interface LoginHistoryDTO {
  id: number;
  loggedAt: string;
  ip: string | null;
  userAgent: string | null;
  result: string;
  accountId: number;
  accountLogin: string;
}
