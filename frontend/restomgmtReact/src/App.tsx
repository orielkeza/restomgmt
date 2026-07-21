import './App.css';
import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { type RootState, type AppDispatch } from './store/store';
import { LoginView } from './features/auth/LoginView';
import { RegistrationView } from './features/auth/RegistrationView';
import { ForgotPasswordView } from './features/auth/ForgotPasswordView';
import { ResetPasswordView } from './features/auth/ResetPasswordView';
import { VerifyEmailView } from './features/auth/VerifyEmailView';
import { CartView } from './features/cart/CartView';
import { MenuView } from './features/menu/MenuView';
import { DashboardView } from './features/dashboard/DashboardView';
import { OrderView } from './features/order/OrderView';
import { UserManagementView } from './features/users/UserManagement';
import { logout, setViewMode } from './features/auth/authSlice';
import { theme } from './theme';

type ViewId = 'dashboard' | 'orders' | 'menu' | 'cart' | 'users';
type AuthViewId = 'login' | 'registration' | 'forgot-password';

const STAFF_NAV_ITEMS: { id: ViewId; label: string; icon: string }[] = [
  { id: 'dashboard', label: 'Dashboard', icon: '▦' },
  { id: 'orders',    label: 'Orders',    icon: '🧾' },
  { id: 'menu',      label: 'Menu',      icon: '🍽' },
  { id: 'users',     label: 'Users',     icon: '👥' },
];

const CUSTOMER_NAV_ITEMS: { id: ViewId; label: string; icon: string }[] = [
  { id: 'menu',   label: 'Menu',      icon: '🍽' },
  { id: 'cart',   label: 'Cart',      icon: '🛒' },
  { id: 'orders', label: 'My Orders', icon: '🧾' },
];

function App() {
  const [currentView, setCurrentView] = useState<ViewId>('menu');
  const [authView, setAuthView] = useState<AuthViewId>('login');
  const dispatch = useDispatch<AppDispatch>();

  const isLoggedIn = useSelector((state: RootState) => state.auth.isLoggedIn);
  const username = useSelector((state: RootState) => state.auth.username);
  const roles = useSelector((state: RootState) => state.auth.roles);
  const viewMode = useSelector((state: RootState) => state.auth.viewMode);

  const canSwitchViews = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_STAFF');
  const navItems = viewMode === 'staff' ? STAFF_NAV_ITEMS : CUSTOMER_NAV_ITEMS;

  //must run on every render, before any early return
  useEffect(() => {
    if (!navItems.some((item) => item.id === currentView)) {
      setCurrentView(navItems[0].id);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [viewMode]);

  // --- URL-based routes that bypass the app shell entirely ---
  const urlParams = new URLSearchParams(window.location.search);
  const verifyToken = window.location.pathname === '/verify-email' ? urlParams.get('token') : null;
  const resetToken = window.location.pathname === '/reset-password' ? urlParams.get('token') : null;

  if (verifyToken) {
    return <VerifyEmailView token={verifyToken} onDone={() => { window.location.href = '/'; }} />;
  }
  if (resetToken) {
    return <ResetPasswordView token={resetToken} onDone={() => { window.location.href = '/'; }} />;
  }

  // --- Unauthenticated ---
  if (!isLoggedIn) {
    if (authView === 'forgot-password') {
      return <ForgotPasswordView onBack={() => setAuthView('login')} />;
    }
    return authView === 'login' ? (
      <LoginView
        onSwitchToRegister={() => setAuthView('registration')}
        onForgotPassword={() => setAuthView('forgot-password')}
      />
    ) : (
      <RegistrationView onSwitchToLogin={() => setAuthView('login')} />
    );
  }

  const renderView = () => {
    switch (currentView) {
      case 'dashboard': return <DashboardView />;
      case 'orders':    return <OrderView />;
      case 'menu':      return <MenuView />;
      case 'cart':      return <CartView />;
      case 'users':     return <UserManagementView />;
      default:          return <MenuView />;
    }
  };

  const activeLabel = navItems.find((n) => n.id === currentView)?.label ?? '';

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: theme.colors.bg, fontFamily: theme.font }}>
      <aside style={{
        width: '230px',
        background: theme.colors.surface,
        borderRight: `1px solid ${theme.colors.border}`,
        padding: '24px 16px',
        display: 'flex',
        flexDirection: 'column',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '0 8px 24px 8px' }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: theme.radius.sm,
            background: theme.colors.brand, color: 'white',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold',
          }}>
            R
          </div>
          <span style={{ fontWeight: 700, fontSize: '16px', color: theme.colors.textPrimary }}>
            Restaurant MS
          </span>
        </div>

        {canSwitchViews && (
          <div style={{
            display: 'flex', background: theme.colors.bg, borderRadius: theme.radius.sm,
            padding: '3px', marginBottom: '20px',
          }}>
            <button
              onClick={() => dispatch(setViewMode('staff'))}
              style={{
                flex: 1, padding: '7px', borderRadius: '6px', border: 'none', cursor: 'pointer',
                fontSize: '12px', fontWeight: 600,
                background: viewMode === 'staff' ? theme.colors.surface : 'transparent',
                color: viewMode === 'staff' ? theme.colors.textPrimary : theme.colors.textMuted,
                boxShadow: viewMode === 'staff' ? theme.shadow.card : 'none',
              }}
            >
              Staff View
            </button>
            <button
              onClick={() => dispatch(setViewMode('customer'))}
              style={{
                flex: 1, padding: '7px', borderRadius: '6px', border: 'none', cursor: 'pointer',
                fontSize: '12px', fontWeight: 600,
                background: viewMode === 'customer' ? theme.colors.surface : 'transparent',
                color: viewMode === 'customer' ? theme.colors.textPrimary : theme.colors.textMuted,
                boxShadow: viewMode === 'customer' ? theme.shadow.card : 'none',
              }}
            >
              Customer View
            </button>
          </div>
        )}

        <nav style={{ flex: 1 }}>
          {navItems.map((item) => {
            const isActive = currentView === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setCurrentView(item.id)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '10px',
                  width: '100%', textAlign: 'left',
                  padding: '10px 12px', marginBottom: '4px',
                  borderRadius: theme.radius.sm, border: 'none', cursor: 'pointer',
                  background: isActive ? theme.colors.brand : 'transparent',
                  color: isActive ? 'white' : theme.colors.textSecondary,
                  fontWeight: isActive ? 600 : 500,
                  fontSize: '14px',
                }}
              >
                <span>{item.icon}</span>
                {item.label}
              </button>
            );
          })}
        </nav>

        <div style={{
          borderTop: `1px solid ${theme.colors.border}`,
          paddingTop: '16px',
          display: 'flex', alignItems: 'center', gap: '10px',
        }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: '50%',
            background: '#FFE4D6', color: theme.colors.brand,
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: '13px',
          }}>
            {username?.[0]?.toUpperCase() ?? 'U'}
          </div>
          <div style={{ fontSize: '13px' }}>
            <div style={{ fontWeight: 600, color: theme.colors.textPrimary }}>{username ?? 'Guest'}</div>
            <div style={{ color: theme.colors.textSecondary }}>
              {viewMode === 'staff' ? 'Admin' : 'Customer view'}
            </div>
          </div>
          <button
            onClick={() => dispatch(logout())}
            title="Log out"
            style={{ marginLeft: 'auto', border: 'none', background: 'none', color: theme.colors.textMuted, cursor: 'pointer', fontSize: '16px' }}
          >
            ⏻
          </button>
        </div>
      </aside>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <header style={{
          height: '64px', background: theme.colors.surface,
          borderBottom: `1px solid ${theme.colors.border}`,
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '0 32px',
        }}>
          <h1 style={{ fontSize: '18px', fontWeight: 700, color: theme.colors.textPrimary, margin: 0 }}>
            {activeLabel}
          </h1>
        </header>

        <main style={{ flex: 1, padding: '32px', overflowY: 'auto' }}>
          {renderView()}
        </main>
      </div>
    </div>
  );
}

export default App;