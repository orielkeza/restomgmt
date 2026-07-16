import './App.css';
import { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { type RootState } from './store/store';
import { LoginView } from './features/auth/LoginView';
import { RegistrationView } from './features/auth/RegistrationView';
import { CartView } from './features/cart/CartView';
import { MenuView } from './features/menu/MenuView';
import { DashboardView } from './features/dashboard/DashboardView';
import { OrderView } from './features/order/OrderView';
import { logout } from './features/auth/authSlice';
import { theme } from './theme';

type ViewId = 'dashboard' | 'orders' | 'menu' | 'cart' | 'booking';
type AuthViewId = 'login' | 'registration';

const NAV_ITEMS: { id: ViewId; label: string; icon: string }[] = [
  { id: 'dashboard', label: 'Dashboard', icon: '▦' },
  { id: 'orders',    label: 'Orders',    icon: '🧾' },
  { id: 'menu',      label: 'Menu',      icon: '🍽' },
  { id: 'cart',      label: 'Cart',      icon: '🛒' },
  { id: 'booking',   label: 'Bookings',  icon: '📅' },
];

function App() {
  const [currentView, setCurrentView] = useState<ViewId>('dashboard');
  const [authView, setAuthView] = useState<AuthViewId>('login');
  const dispatch = useDispatch();

  const isLoggedIn = useSelector((state: RootState) => state.auth.isLoggedIn);
  const username = useSelector((state: RootState) => state.auth.username);

  if (!isLoggedIn) {
    return authView === 'login' ? (
      <LoginView onSwitchToRegister={() => setAuthView('registration')} />
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
      default:          return <DashboardView />;
    }
  };

  const activeLabel = NAV_ITEMS.find((n) => n.id === currentView)?.label ?? 'Dashboard';

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: theme.colors.bg, fontFamily: theme.font }}>
      {/* Sidebar */}
      <aside style={{
        width: '230px',
        background: theme.colors.brandDark,
        padding: '24px 16px',
        display: 'flex',
        flexDirection: 'column',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '0 8px 32px 8px' }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: theme.radius.sm,
            background: theme.colors.brand, color: 'white',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold',
          }}>
            R
          </div>
          <span style={{ fontWeight: 700, fontSize: '16px', color: 'white', letterSpacing: '0.2px' }}>
            Restaurant MS
          </span>
        </div>

        <nav style={{ flex: 1 }}>
          {NAV_ITEMS.map((item) => {
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
                  color: isActive ? 'white' : 'rgba(255,255,255,0.75)',
                  fontWeight: isActive ? 600 : 500,
                  fontSize: '14px',
                  transition: 'background 0.15s ease, color 0.15s ease',
                }}
              >
                <span>{item.icon}</span>
                {item.label}
              </button>
            );
          })}
        </nav>

        <div style={{
          borderTop: '1px solid rgba(255,255,255,0.12)',
          paddingTop: '16px',
          display: 'flex', alignItems: 'center', gap: '10px',
        }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: '50%',
            background: theme.colors.brandDark, color: theme.colors.brand,
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: '13px',
          }}>
            {username?.[0]?.toUpperCase() ?? 'U'}
          </div>
          <div style={{ fontSize: '13px' }}>
            <div style={{ fontWeight: 600, color: 'white' }}>{username ?? 'Guest'}</div>
            <div style={{ color: 'rgba(255,255,255,0.6)' }}>Admin</div>
          </div>
          <button
            onClick={() => dispatch(logout())}
            title="Log out"
            style={{
              marginLeft: 'auto', border: 'none', background: 'none',
              color: 'rgba(255,255,255,0.6)', cursor: 'pointer', fontSize: '16px',
            }}
          >
            ⏻
          </button>
        </div>
      </aside>

      {/* Main column */}
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