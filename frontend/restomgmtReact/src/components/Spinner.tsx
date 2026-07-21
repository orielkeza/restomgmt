import React from 'react';

interface SpinnerProps {
  size?: number;
  color?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({ size = 16, color = 'currentColor' }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" style={{ animation: 'app-spin 0.8s linear infinite', flexShrink: 0 }}>
    <circle
      cx="12" cy="12" r="10"
      stroke={color} strokeWidth="3" fill="none"
      strokeDasharray="31.4 31.4" strokeLinecap="round" opacity={0.9}
    />
  </svg>
);