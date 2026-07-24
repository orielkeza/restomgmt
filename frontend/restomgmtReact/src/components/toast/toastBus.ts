type ToastKind = 'success' | 'error' | 'info';
export interface ToastMessage {
  id: number;
  text: string;
  kind: ToastKind;
}

type Listener = (toast: ToastMessage) => void;
const listeners: Listener[] = [];
let counter = 0;

export function showToast(text: string, kind: ToastKind = 'info') {
  const toast: ToastMessage = { id: ++counter, text, kind };
  listeners.forEach((l) => l(toast));
}

export function subscribeToasts(listener: Listener): () => void {
  listeners.push(listener);
  return () => {
    const idx = listeners.indexOf(listener);
    if (idx !== -1) listeners.splice(idx, 1);
  };
}