package com.mycompany.app;

class SortAccounts {

    private static final int MAX_INSERTION_SORT_SIZE = 20;

    static boolean compare(Account a, Account b) {

        int c = Long.compareUnsigned(a.account1, b.account1);
        if (c == 0) {
            return Long.compareUnsigned(a.account2, b.account2) < 0;
        }
        return c < 0;

    }

    static boolean compareEq(Account a, Account b) {
        int c = Long.compareUnsigned(a.account1, b.account1);
        if (c == 0) {
            return Long.compareUnsigned(a.account2, b.account2) <= 0;
        }
        return c < 0;
    }

    public static void quickSort(Account[] arr) {
        sortDual(arr, 0, arr.length - 1);
    }

    static void sortDual(Account[] A, int left, int right) {

        if (right - left <= MAX_INSERTION_SORT_SIZE) {
            insertionSort(A, left, right);
            return;
        }

        if (right > left) {
            // Choose outermost elements as pivots
            if (compare(A[right], A[left]))
                swap(A, left, right);
            Account p = A[left], q = A[right];

            // Partition A according to invariant below
            int l = left + 1, g = right - 1, k = l;
            while (k <= g) {
                if (compare(A[k], p)) {
                    swap(A, k, l);
                    ++l;
                } else if (compareEq(q, A[k])) {
                    while (compare(q, A[g]) && k < g)
                        --g;
                    swap(A, k, g);
                    --g;
                    if (compare(A[k], p)) {
                        swap(A, k, l);
                        ++l;
                    }
                }
                ++k;
            }
            --l;
            ++g;

            // Swap pivots to final place
            swap(A, left, l);
            swap(A, right, g);

            // Recursively sort partitions
            sortDual(A, left, l - 1);
            sortDual(A, l + 1, g - 1);
            sortDual(A, g + 1, right);
        }
    }

    static void swap(Account[] A, int i, int j) {
        final Account tmp = A[i];
        A[i] = A[j];
        A[j] = tmp;
    }

    private static void insertionSort(Account[] a, int low, int high) {
        for (int i, k = low; ++k < high;) {
            Account ai = a[i = k];

            if (compare(ai, a[i - 1])) {
                while (--i >= low && compare(ai, a[i])) {
                    a[i + 1] = a[i];
                }
                a[i + 1] = ai;
            }
        }
    }

}

class SortClans {

    private static final int MAX_INSERTION_SORT_SIZE = 30;

    static boolean compare(Object aa, Object bb) {
        Clan a = (Clan) aa;
        Clan b = (Clan) bb;

        int p = b.points - a.points;
        if (p != 0) {
            return p < 0;
        }
        return a.numberOfPlayers < b.numberOfPlayers;

    }

    static boolean compareEq(Object aa, Object bb) {
        Clan a = (Clan) aa;
        Clan b = (Clan) bb;
        int p = b.points - a.points;
        if (p != 0) {
            return p < 0;
        }
        return a.numberOfPlayers <= b.numberOfPlayers;
    }

    public static void sort(MyArray<Clan> arr) {
        sortDual((Object[]) arr.data, 0, arr.size - 1);

    }

    static void sortDual(Object[] A, int left, int right) {

        if (right - left <= MAX_INSERTION_SORT_SIZE) {
            insertionSort(A, left, right);
            return;
        }

        if (right > left) {
            // Choose outermost elements as pivots
            if (compare(A[right], A[left]))
                swap(A, left, right);
            Object p = A[left], q = A[right];

            // Partition A according to invariant below
            int l = left + 1, g = right - 1, k = l;
            while (k <= g) {
                if (compare(A[k], p)) {
                    swap(A, k, l);
                    ++l;
                } else if (compareEq(q, A[k])) {
                    while (compare(q, A[g]) && k < g)
                        --g;
                    swap(A, k, g);
                    --g;
                    if (compare(A[k], p)) {
                        swap(A, k, l);
                        ++l;
                    }
                }
                ++k;
            }
            --l;
            ++g;

            // Swap pivots to final place
            swap(A, left, l);
            swap(A, right, g);

            // Recursively sort partitions
            sortDual(A, left, l - 1);
            sortDual(A, l + 1, g - 1);
            sortDual(A, g + 1, right);
        }
    }

    static void swap(Object[] A, int i, int j) {
        final Object tmp = A[i];
        A[i] = A[j];
        A[j] = tmp;
    }

    private static void insertionSort(Object[] a, int low, int high) {
        for (int i, k = low; ++k < high;) {
            Object ai = a[i = k];

            if (compare(ai, a[i - 1])) {
                while (--i >= low && compare(ai, a[i])) {
                    a[i + 1] = a[i];
                }
                a[i + 1] = ai;
            }
        }
    }

}
