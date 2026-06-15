import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { loginSuccess } from './authSlice';

export const RegistrationView: React.FC = () => {
    const dispatch = useDispatch();

    //keeps track of what the user types in real-time

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [email, setEmail] = useState('');
    const [error, setError] = useState('');

    //handles submission

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault(); //prevent from reloading the entire page

        if(username.trim() === '' || password.trim() === '' || email.trim() === ''){ //=== is strict equality instead of loose equality
            setError('Please fill in all the fields');
            return;
        }

        setError('');

        //sends he action to redux so that the user is logged in globally

        dispatch(loginSuccess(username));
        alert('Signed Up as ${username}');
    };

    return (
        <div style={{
            backgroundColor: '#E70B0D',
            minHeight: '100vh', //elemtn must be at least as tall as the visible screen
            display: 'flex', //makes all child elements flexible 
            justifyContent: 'center',
            alignItems: 'center',
            fontFamily: 'sans-serif'
        }}>
            {}
            <div style={{
                backgroundColor: 'white',
                padding: '40px',
                borderRadius: '12px',
                width: '320px',
                boxShadow: '0px 8px 16px rgba(0,0,0,1)',
                textAlign: 'center'
            }}>
                <h1 style={{ margin: '0 0 4px 0', fontSize: '24px', color: '#333' }}>Restaurant Management</h1>
                <h2 style={{ margin: '0 0 24px 0', fontSize: '18px', color: '#666' }}>Registration</h2>

                {}
                <button style={{
                    width: '100%',
                    padding: '10px',
                    backgroundColor: 'f5f5f5',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    marginBottom: '20px',
                    fontSize: '14px'
                }}>
                    Login with Google
                </button>

                {error && <p style={{ color: 'red', fontSize: '14px'}}>{error}</p>}

                {}
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px'}}>
                    <input
                        type="text"
                        placeholder="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        style={inputStyle}
                    />
                    <input
                        type="text"
                        placeholder="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={inputStyle}
                    />
                    <input
                        type="password"
                        placeholder="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={inputStyle}
                    />
                    {}
                <button style={{
                    width: '100%',
                    padding: '10px',
                    backgroundColor: 'f5f5f5',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    marginBottom: '20px',
                    fontSize: '14px'
                }}>
                    Create account
                </button>
                </form>
            </div>
        </div>
    );
};

//reusable basic cc object for inputs

const inputStyle: React.CSSProperties = {
    padding: '12px',
    borderRadius: '6px',
    border: '1px solid #ccc',
    fontSize: '14px',
    backgroundColor: '#f9f9f9'
};