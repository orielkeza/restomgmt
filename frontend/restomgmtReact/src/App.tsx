import './App.css';
import {useState} from 'react';
import { LoginView } from './features/auth/LoginView';
import { RegistrationView } from './features/auth/RegistrationView';

function App() {
//local state to track which screen we want to view rn
const [currentView, setCurrentView] = useState<'login' | 'register'>('login');

  return (
    <div style={{ position: 'relative' }}>
      {}
      <div style={{
        position: 'absolute',
        top: '10px',
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 1000,
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        padding: '8px 16px',
        borderRadius: '20px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        display: 'flex',
        gap: '12px'
      }}>
        <button 
          onClick={() => setCurrentView('login')}
          style={{ fontWeight: currentView === 'login' ? 'bold' : 'normal', cursor: 'pointer' }}
        >
          View Login
        </button>
        <button
          onClick={()=> setCurrentView('registration')}
          style={{ fontWeight: currentView === 'registration' ? 'bold' : 'normal
      </div>
    </div>
  )
}

export default App