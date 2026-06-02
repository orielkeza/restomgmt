import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';


//redux brain, a slice is a small part of the global store that is dedicated to one feature

//tells ts what data this slice needs to remeber and keep track of
interface AuthState {
    username: string | null;
    isLoggedIn: boolean;
    loading: boolean;
}

//initial state for when the app loads up first
const initialState: AuthState = {
    username: null,
    isLoggedIn: false,
    loading:false,
};

//creating the slice
export const authSlice = createSlice ({
    name: 'auth',
    initialState,
    //reducers are what allow us to modify our state
    reducers: {
        loginSuccess: (state, action: PayloadAction<string>) => {
            state.isLoggedIn = true;
            state.username = action.payload;
        },
        logout: (state) => {
            state.isLoggedIn = false;
            state.username = null;
        },
    },
});

//export the actions so that the components can be triggered
export const { loginSuccess, logout } = authSlice.actions;

//export the reducer so that the global store can register it
export default authSlice.reducer;