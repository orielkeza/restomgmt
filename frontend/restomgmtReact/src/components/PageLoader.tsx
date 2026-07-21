import React from 'react';
import { Spinner } from './Spinner';
import { theme } from '../theme';

interface PageLoaderProps {
  label?: string;
  compact?: boolean; // use for section-level loaders inside an already-loaded page
}

export const PageLoader: React.FC<PageLoaderProps> = ({ label = 'Loading…', compact = false }) => (
  <div style={{
    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
    gap: '12px', padding: compact ? '24px' : '60px',
    color: theme.colors.textSecondary, fontFamily: theme.font, fontSize: '14px',
  }}>
    <Spinner size={compact ? 20 : 28} color={theme.colors.brand} />
    <span>{label}</span>
  </div>
);