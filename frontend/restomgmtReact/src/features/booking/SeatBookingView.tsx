import React, { useState } from 'react';
import { useDispatch } from 'react-redux';

export const SeatBookingView: React.FC = () => {
    const dispatch = useDispatch();

    //keeps track of what the user types in real-time

    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [tAndD, setTAndD] = useState('');
    const [guests, setGuests] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    //handles submission

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault(); //prevent from reloading the entire page

        if(name.trim() === ''){
            setError('Please fill in name');
            return;
        }

         if(email.trim() === ''){
            setError('Please fill in email');
            return;
        }

        if(tAndD.trim() === ''){
            setError('Please fill in the time and date');
            return;
        }

        if(guests.trim() === ''){
            setError('Please fill in the number of guests');
            return;
        }

        setError('');
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
                width: '600px',
                boxShadow: '0px 8px 16px rgba(0,0,0,1)',
                textAlign: 'center'
            }}>
                <h1 style={{ margin: '0 0 4px 0', fontSize: '24px', color: '#333' }}>Restaurant Management</h1>
                <h2 style={{ margin: '0 0 24px 0', fontSize: '18px', color: '#666' }}>Seat Booking</h2>

                {}
                {error && <p style={{ color: 'red', fontSize: '14px'}}>{error}</p>}

                {}
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px'}}>

                    <div style={{
                        display: 'flex',
                        gap: '12px'
                    }}>

                    <input
                        type="text"
                        placeholder="name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        style={{ ...inputStyle,
                                 flex: 1
                        }}
                    />
                    <input
                        type="email"
                        placeholder="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        style={{ ...inputStyle,
                                 flex: 1
                        }}
                    />

                    </div>
                    <div style={{
                        display: 'flex',
                        gap: '12px'
                    }}>

                    <input
                        type="date"
                        placeholder="time and date"
                        value={tAndD}
                        onChange={(e) => setTAndD(e.target.value)}
                        style={{ ...inputStyle,
                                 flex: 1
                        }}
                    />
                    <input
                        type="number"
                        placeholder="number of guests"
                        value={guests}
                        onChange={(e) => setGuests(e.target.value)}
                        style={{ ...inputStyle,
                                 flex: 1
                        }}
                    />
                    
                    </div>
                    <div style={{
                        display: 'flex',
                        gap: '12px'
                    }}>
                    <input
                        type="text"
                        placeholder="message"
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        style={{ ...inputStyle,
                                 flex: 1
                        }}
                    />

                    </div>
                    <button type="submit" style={{
                        backgroundColor: '#E00B0D',
                        color: 'white',
                        border: 'none',
                        padding: '12px',
                        borderRadius: '6px',
                        fontWeight: 'bold',
                        cursor: 'pointer',
                        marginTop: '10px'
                    }}>
                        Confirm your reservation
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