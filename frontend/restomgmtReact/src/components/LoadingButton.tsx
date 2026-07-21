import React from 'react';
import { Spinner } from './Spinner';

interface LoadingButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  loading?: boolean;
  loadingText?: string;
  spinnerColor?: string;
}

// Drop-in replacement for <button> — pass the same style prop as before,
// plus `loading`. Handles disabling + spinner + text swap consistently everywhere.
export const LoadingButton: React.FC<LoadingButtonProps> = ({
  loading = false,
  loadingText,
  spinnerColor = 'currentColor',
  children,
  disabled,
  style,
  ...rest
}) => (
  <button
    disabled={loading || disabled}
    style={{
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      opacity: loading ? 0.75 : 1,
      cursor: loading || disabled ? 'not-allowed' : 'pointer',
      ...style,
    }}
    {...rest}
  >
    {loading && <Spinner size={14} color={spinnerColor} />}
    {loading && loadingText ? loadingText : children}
  </button>
);