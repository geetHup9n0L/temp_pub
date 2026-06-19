import java.util.ArrayList;

public class Leetcode {

    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        int i = 0, val;
        ArrayList<Integer> list = new ArrayList<>();
        while(l1.next != null && l2.next != null) {
            val = 0;
            if(l1.val == null) {

            }
            list[i] += (l1.val + l2.val);
        }
        return null;
    }

    public static void main(String[] args) {
        String num1 = "11221";
        String num2 = "21231";
//        int len1 = Integer.parseInt(num1);
//        int len2 = Integer.parseInt(num2);
        ListNode head1 = createListNode(num1);
        ListNode head2 = createListNode(num2);
        printList(head1);
        printList(head2);
        ListNode result = addTwoNumbers(head1, head2);
    }

    public static ListNode createListNode(String num) {
        ListNode head = null;
        int val;
        for(int i=num.length()-1;i>=0;i--) {
            val = Integer.parseInt(String.valueOf(num.charAt(i)));
            if(head == null) {
                head = new ListNode(val);
            }
            ListNode node = new ListNode(val, head);
            head = node;
        }
        return head;
    }

    public static void printList(ListNode head) {
        ListNode temp = head;
        while(temp.next != null) {
            System.out.print(temp.val + " ");
            temp = temp.next;
        }
        System.out.println("");
    }
}
