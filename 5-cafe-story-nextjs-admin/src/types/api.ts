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
      detail?: string;
      error?: string;
      message?: string;
      path?: string;
      status?: number | string;
      timestamp?: string;
      title?: string;
    };

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty?: boolean;
  numberOfElements?: number;
  pageable?: unknown;
  sort?: unknown;
};
