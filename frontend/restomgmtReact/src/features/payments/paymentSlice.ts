import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { paymentApi, type PaymentResponse } from '../../api/paymentApi';
import type { RootState } from '../../store/store';

interface PaymentState {
    currentPayment: PaymentResponse | null;
    initiateStatus: 'idle' | 'loading' | 'failed';
    pollStatus: 'idle' | 'polling' | 'done' | 'timeout' | 'failed';
    error: string | null;
}

const initialState: PaymentState = {
    currentPayment: null,
    initiateStatus: 'idle',
    pollStatus: 'idle',
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

export const checkPaymentStatus = createAsyncThunk<PaymentResponse, number, ThunkConfig>(
    'payments/checkPaymentStatus',
    async (orderId, { getState, rejectWithValue }) => {
        try {
            return await paymentApi.checkStatus(getState().auth.token, orderId);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to check payment status');
        }
    }
);

export const flagRefund = createAsyncThunk<PaymentResponse, number, ThunkConfig>(
    'payments/flagRefund',
    async (orderId, { getState, rejectWithValue }) => {
        try {
            return await paymentApi.flagRefund(getState().auth.token, orderId);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to flag refund');
        }
    }
);

export const paymentSlice = createSlice({
    name: 'payments',
    initialState,
    reducers: {
        resetPayment: (state) => {
            state.currentPayment = null;
            state.initiateStatus = 'idle';
            state.pollStatus = 'idle';
            state.error = null;
        },
        setPollTimeout: (state) => {
            state.pollStatus = 'timeout';
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(initiatePayment.pending, (state) => {
                state.initiateStatus = 'loading';
                state.error = null;
            })
            .addCase(initiatePayment.fulfilled, (state, action) => {
                state.initiateStatus = 'idle';
                state.currentPayment = action.payload;
                state.pollStatus = 'polling';
            })
            .addCase(initiatePayment.rejected, (state, action) => {
                state.initiateStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to start payment';
            })
            .addCase(checkPaymentStatus.fulfilled, (state, action) => {
                state.currentPayment = action.payload;
                if (action.payload.status !== 'PENDING') {
                    state.pollStatus = 'done';
                }
            })
            .addCase(checkPaymentStatus.rejected, (state, action) => {
                state.pollStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to check payment status';
            })
            .addCase(flagRefund.fulfilled, (state, action) => {
                state.currentPayment = action.payload;
            });
    },
});

export const { resetPayment, setPollTimeout } = paymentSlice.actions;
export default paymentSlice.reducer;