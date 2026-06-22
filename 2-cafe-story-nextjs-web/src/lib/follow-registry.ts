type Listener = (following: boolean) => void;

const _state = new Map<string, boolean>();
const _listeners = new Map<string, Set<Listener>>();

function _key(type: "user" | "cafe", id: string) {
  return `${type}:${id}`;
}

export const followRegistry = {
  get(type: "user" | "cafe", id: string): boolean | null {
    const k = _key(type, id);
    return _state.has(k) ? (_state.get(k) as boolean) : null;
  },

  set(type: "user" | "cafe", id: string, following: boolean) {
    const k = _key(type, id);
    _state.set(k, following);
    _listeners.get(k)?.forEach((fn) => fn(following));
  },

  subscribe(type: "user" | "cafe", id: string, fn: Listener) {
    const k = _key(type, id);
    if (!_listeners.has(k)) _listeners.set(k, new Set());
    _listeners.get(k)!.add(fn);
    return () => { _listeners.get(k)?.delete(fn); };
  },
};
