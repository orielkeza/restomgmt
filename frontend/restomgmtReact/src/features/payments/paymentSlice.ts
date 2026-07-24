import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { paymentApi, type PaymentResponse } from '../../api/paymentApi';
import type { RootState } from '../../store/store';

interface PaymentState {
    paymentsByOrderId: Record<number, PaymentResponse>;
    initiateStatus: 'idle' | 'loading' | 'failed';
    fetchStatus: 'idle' | 'loading' | 'failed';
    updateStatus: 'idle' | 'loading' | 'failed';
    error: string | null;
}

const initialState: PaymentState = {
    paymentsByOrderId: {},
    initiateStatus: 'idle',
    fetchStatus: 'idle',
    updateStatus: 'idle',
    error: null,
};

type ThunkConfig = { state: RootState };

export const initiatePayment = createAsyncThunk<
    PaymentResponse,
    { orderId: number; payerPhone: string },
    ThunkConfig
>('payments/initiatePayment', async ({ orderId, payerPhone }, { getState, rejectWithValue }) => {
    try {
        return await paymentApi.initiatePayment(getState().auth.token, orderId, payerPhone);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to start payment');
    }
});

export const fetchPaymentForOrder = createAsyncThunk<PaymentResponse, number, ThunkConfig>(
    'payments/fetchPaymentForOrder',
    async (orderId, { getState, rejectWithValue }) => {
        try {
            return await paymentApi.getPayment(getState().auth.token, orderId);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'No payment found for this order');
        }
    }
);

export const setPaymentStatus = createAsyncThunk<
    PaymentResponse,
    { orderId: number; status: 'SUCCESSFUL' | 'PENDING' },
    ThunkConfig
>('payments/setPaymentStatus', async ({ orderId, status }, { getState, rejectWithValue }) => {
    try {
        return await paymentApi.setPaymentStatus(getState().auth.token, orderId, status);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to update payment status');
    }
});

export const paymentSlice = createSlice({
    name: 'payments',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(initiatePayment.pending, (state) => { state.initiateStatus = 'loading'; state.error = null; })
            .addCase(initiatePayment.fulfilled, (state, action) => {
                state.initiateStatus = 'idle';
                state.paymentsByOrderId[action.payload.orderId] = action.payload;
            })
            .addCase(initiatePayment.rejected, (state, action) => {
                state.initiateStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to start payment';
            })
            .addCase(fetchPaymentForOrder.pending, (state) => { state.fetchStatus = 'loading'; })
            .addCase(fetchPaymentForOrder.fulfilled, (state, action) => {
                state.fetchStatus = 'idle';
                state.paymentsByOrderId[action.payload.orderId] = action.payload;
            })
            .addCase(fetchPaymentForOrder.rejected, (state, action) => {
                state.fetchStatus = 'failed';
                state.error = (action.payload as string) ?? 'No payment found';
            })
            .addCase(setPaymentStatus.pending, (state) => { state.updateStatus = 'loading'; })
            .addCase(setPaymentStatus.fulfilled, (state, action) => {
                state.updateStatus = 'idle';
                state.paymentsByOrderId[action.payload.orderId] = action.payload;
            })
            .addCase(setPaymentStatus.rejected, (state, action) => {
                state.updateStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to update payment status';
            });
    },
});

export default paymentSlice.reducer;