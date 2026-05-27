export type ApiStatus = "Success" | "Fail";

export type ApiEnvelope<T> = {
  statusCode: number;
  status: ApiStatus;
  message: string;
  data: T;
};

export type ApiErrorPayload = ApiEnvelope<null> | {
  detail?: string;
  error?: string;
  message?: string;
  path?: string;
  status?: number | string;
  timestamp?: string;
  title?: string;
};
