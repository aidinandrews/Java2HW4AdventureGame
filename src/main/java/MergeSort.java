import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MergeSort {

    public static void mystery(Map<String, String> m){
        Set<String>s=new TreeSet<String>();
for (String key:m.keySet()){
    if(!m.get(key).equals(key)){
        s.add(m.get(key));
    }else{
        s.remove(m.get(key));
    }
}System.out.println(s);
    }

    public static void main(String[] args) {
        Map<String, String> m = new HashMap<>();
        m.put("munchkin","blue");
        m.put("winkie","yellow");
        m.put("corn","yellow");
        m.put("grass","green");
        m.put( "emerald","green");
        mystery(m);

    }

    public static void mergeSort(int[] a) {
        helper(a, 0, a.length);
    }

    static void printArray(int[] a) {
        for (int i : a) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
    private static void helper(int[] a, int first, int last) {
        if (first < last) {
            int mid = first + (last - first) / 2;
            System.out.print("helper: first: " + first + ", last: " + last + " | ");
            printArray(a);
            helper(a, first, mid);

            System.out.print("helper: first: " + first + ", last: " + last + " | ");
            printArray(a);
            helper(a, mid+1, last);

            merge(a, first, mid, last);
        }
    }

    private static void merge(int[] a, int first, int mid, int last) {
        int i1 = first, i2 = mid + 1;

        System.out.print("before merge: i1: " + i1 + ", mid: " + mid + ", i2: " + i2 + " | ");
        printArray(a);
        last = Math.min(a.length - 1, last);
    if (i2 == a.length) {
        System.out.println("returning!");
        return;
    }
        while (i1 <= last && i2 <= last) {
            System.out.println("i1: " + i1 + " , i2: " + i2);

            if (a[i1] <= a[i2]) {
                i1++;
                if (i1 == i2) {
                    i2++;
                }
            }
            else {
                int temp = a[i1];
                a[i1] = a[i2];
                a[i2] = temp;
                i1++;
                i2++;
                if (i2 > last) i2=last;
            }

        }
        System.out.print("after merge: i1: " + i1 + ", mid: " + mid + ", i2: " + i2 + " | ");
        printArray(a);
//        for (int i : a) {
//            System.out.print(i + " ");
//        }
//        System.out.println();

    }
}
