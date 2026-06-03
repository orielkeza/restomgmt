import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { loginSuccess } from './authSlice';

export const CartView: React.FC = () => {
    const dispatch = useDispatch();

    //keeps track of what the user types in real-time

    const [error, setError] = useState('');

    //handles submission

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault(); //prevent from reloading the entire page

        setError('');

        //sends he action to redux so that the user is logged in gl');
    };

    interface RowData {
        id: number;
        item: string;
        quantity: number;
        price: number;
    }

    const sampleData: RowData[] = [
        { id: 1, item: 'Chicken Wings', quantity: 1, price: 25 },
        { id: 2, item: 'Virgin Mojito', quantity: 2, price: 8 },
        { id: 3, item: 'Fruit Salad', quantity: 2, price: 12 },
    ];

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
                borderRadius: '0px',
                width: '1000px',
                height: '750px',
                boxShadow: '0px 8px 16px rgba(0,0,0,1)',
                textAlign: 'center'
            }}>
                <h1 style={{ margin: '0 0 4px 0', fontSize: '24px', color: '#333' }}>Cart</h1>                
                {error && <p style={{ color: 'red', fontSize: '14px'}}>{error}</p>}

                {}
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px'}}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        {/* Table Header */}
                        <thead>
                        <tr style={{ borderBottom: '2px solid #ccc' }}>
                            <th style={{ padding: '12px' }}>Items</th>
                            <th style={{ padding: '12px' }}>Quantity</th>
                            <th style={{ padding: '12px' }}>Status</th>
                        </tr>
                        </thead>

                        {/* Table Body */}
                        <tbody>
                        {sampleData.map((row) => (
                            <tr key={row.id} style={{ borderBottom: '1px solid #eee' }}>
                            <td style={{ padding: '12px' }}>{row.item}</td>
                            <td style={{ padding: '12px' }}>{row.quantity}</td>
                            <td style={{ padding: '12px' }}>{row.price}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                    <button type="submit" style={{
                        backgroundColor: '#FFCC00',
                        color: 'white',
                        border: 'none',
                        padding: '20px',
                        borderRadius: '6px',
                        fontWeight: 'bold',
                        cursor: 'pointer',
                        marginTop: '20px'
                    }}>
                        Log In
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