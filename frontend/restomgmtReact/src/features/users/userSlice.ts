import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { userApi, type UserResponse } from '../../api/userApi';
import type { RootState } from '../../store/store';

interface UserState {
    users: UserResponse[];
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    pendingUserIds: number[];
    mutationStatus: 'idle' | 'loading' | 'failed';
    error: string | null;
}

const initialState: UserState = { users: [], status: 'idle', pendingUserIds: [], mutationStatus: 'idle', error: null };
type ThunkConfig = { state: RootState };

export const fetchUsers = createAsyncThunk<UserResponse[], void, ThunkConfig>(
    'users/fetchUsers',
    async (_, { getState, rejectWithValue }) => {
        try { return await userApi.getAllUsers(getState().auth.token); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to load users'); }
    }
);

export const deleteUser = createAsyncThunk<number, number, ThunkConfig>(
    'users/deleteUser',
    async (userId, { getState, rejectWithValue }) => {
        try { await userApi.deleteUser(getState().auth.token, userId); return userId; }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to delete user'); }
    }
);

export const assignRole = createAsyncThunk<UserResponse, { userId: number; roleName: string }, ThunkConfig>(
    'users/assignRole',
    async ({ userId, roleName }, { getState, rejectWithValue }) => {
        try { return await userApi.assignRole(getState().auth.token, userId, roleName); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to assign role'); }
    }
);

function addPending(state: UserState, id: number) {
    if (!state.pendingUserIds.includes(id)) state.pendingUserIds.push(id);
}
function removePending(state: UserState, id: number) {
    state.pendingUserIds = state.pendingUserIds.filter((i) => i !== id);
}

export const userSlice = createSlice({
    name: 'users',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchUsers.pending, (state) => { state.status = 'loading'; state.error = null; })
            .addCase(fetchUsers.fulfilled, (state, action) => { state.status = 'succeeded'; state.users = action.payload; })
            .addCase(fetchUsers.rejected, (state, action) => { state.status = 'failed'; state.error = (action.payload as string) ?? 'Failed to load users'; })
            .addCase(deleteUser.pending, (state, action) => addPending(state, action.meta.arg))
            .addCase(deleteUser.fulfilled, (state, action) => {
                removePending(state, action.payload);
                state.users = state.users.filter((u) => u.id !== action.payload);
            })
            .addCase(deleteUser.rejected, (state, action) => {
                removePending(state, action.meta.arg);
                state.error = (action.payload as string) ?? 'Failed to delete user';
            })
            .addCase(assignRole.pending, (state, action) => addPending(state, action.meta.arg.userId))
            .addCase(assignRole.fulfilled, (state, action) => {
                removePending(state, action.payload.id);
                const idx = state.users.findIndex((u) => u.id === action.payload.id);
                if (idx !== -1) state.users[idx] = action.payload;
            })
            .addCase(assignRole.rejected, (state, action) => {
                removePending(state, action.meta.arg.userId);
                state.error = (action.payload as string) ?? 'Failed to assign role';
            });
    },
});

export default userSlice.reducer;