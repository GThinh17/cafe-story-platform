export type ApiStatus = "Success" | "Fail";

export type ApiEnvelope<T> = {
  statusCode: number;
  status: ApiStatus;
  message: string;
  data: T;
};

export type ApiErrorPayload =
  | ApiEnvelope<null>
  | {
      message?: string;
    };
