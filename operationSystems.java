package ����ϵͳ;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

//�����ࣻ
class process {
	int pid;// ���̺�
	int blocksize;// �߼��ռ��С
	int pagenum;// �߼�ҳ����
	String hexagon;// �����߼���ַ
}

// ҳ�����ࣻ 
class pgtabitem {
	int item_id;// ҳ�������
	int hashnum;// ɢ��ֵ
	int pid;// ���̺�
	int pageid;// ҳ���
	int physicid;// ҳ���
	int crashcount;// ��ͻ����
	int state;// ����ռ�ñ�־
	int bitcount;// �ռ��С
}

public class operationSystems extends JFrame {
	Container contentPane = getContentPane();
	JButton submit = new JButton("��������");
	JButton clear = new JButton("����ͷ��ڴ�");
	JTable itemTable;
	JTable processTable;
	JTable processList;
	JPanel choose = new JPanel();
	DefaultTableModel item_model = new DefaultTableModel();
	DefaultTableModel processList_model = new DefaultTableModel();
	JComboBox<String> physicalSpaceCombo = new JComboBox<String>(new String[] { "256MB", "512MB" });
	JComboBox<String> pageSizeCombo = new JComboBox<String>(new String[] { "1KB", "2KB", "4KB" });
	Random random = new Random();
	int hashnum;// ɢ��ֵ��
	int pagesize;// ҳ���С����GUI�ֶ����룻
	String physicsize;// �����ڴ��С����GUIѡ�����룻
	LinkedList<process> List = new LinkedList<process>();// �Զ����ɽ�������
	pgtabitem[] itemList = new pgtabitem[2 << 19];// �ѷ���ҳ�������飻
	Vector process_col = new Vector();
	Vector item_col = new Vector();

	operationSystems() {
		super("����ҳ���ģ��ʵ��");
		contentPane.add(choose, BorderLayout.NORTH);
		choose.add(physicalSpaceCombo);
		choose.add(pageSizeCombo);
		choose.add(submit);
		choose.add(clear);
		process_col.add("���̺�");
		process_col.add("�߼��ռ��С/KB");
		process_col.add("�ɷ�������ռ�");
		process_col.add("�߼���ַ");
		item_col.add("ҳ�������");
		item_col.add("���̺�");
		item_col.add("�߼�ҳ���");
		item_col.add("ҳ���");
		item_col.add("ɢ��ֵ");
		item_col.add("��ͻ����");
		item_col.add("ռ�ñ�־:1��ռ�� /0������");
		item_col.add("ҳ������ռ�ռ�");
		item_model.setColumnIdentifiers(item_col);
		processList_model.setColumnIdentifiers(process_col);
		itemTable = new JTable(item_model);
		processList = new JTable(processList_model);
		JScrollPane item_js = new JScrollPane(itemTable);
		JScrollPane list_js = new JScrollPane(processList);
		contentPane.add(item_js, BorderLayout.CENTER);
		contentPane.add(list_js, BorderLayout.SOUTH);
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton jb = (JButton) e.getSource();
				physicsize = (String) physicalSpaceCombo.getSelectedItem();
				String page = (String) pageSizeCombo.getSelectedItem();
				String page1 = page.substring(0, 1);
				pagesize = Integer.parseInt(page1);
				run();
			}
		});
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton jb = (JButton) e.getSource();
				item_model.setRowCount(0);
				processList_model.setRowCount(0);
				clear();
			}
		});

	}

	// ��պ�����clear();
	public void clear() {
		List.clear();
		for (int i = 0; i < itemList.length; i++) {
			itemList[i] = null;
		}
	}

	// submit���o���ܺ�����
	public void run() {
		createProcess();
		createItemTable();
		createProcessList();
	}

	// ������ѹ������
	public void createProcessList() {
		for (Iterator<process> it = List.iterator(); it.hasNext();) {
			process pro = it.next();
			Vector hang = new Vector();
			hang.add(pro.pid);// ѹ����̺�
			hang.add(pro.blocksize);// ˳������߼�ҳ��
			hang.add(physicsize);// �ɷ�������ռ�
			hang.add("0XFFFFFFFFF" + pro.hexagon);
			processList_model.addRow(hang);// ��ɢ��ֵ��λ��ѹ��ҳ���
		}

	}

	// ��ҳ�����������
	public void createItemTable() {
		int count = 0;
		int number = 1;

		for (Iterator<process> it = List.iterator(); it.hasNext();) {
			process pro = it.next();
			for (int i = count + 1; i < (pro.pagenum + count + 1); i++) {
				pgtabitem item = new pgtabitem();
				item.item_id = number;
				item.pid = pro.pid;
				item.pageid = 4 * i;
				item.physicid = 4 * i;
				item.hashnum = (pro.pid) * (pagesize) + item.pageid;
				item.crashcount = 0;
				item.state = 1;
				item.bitcount = 28;
				// �ж�ɢ��ֵ�Ƿ��ͻ��
				if (itemList[item.hashnum] != null) {
					for (int j = item.hashnum; j < itemList.length; j++) {
						if (itemList[j] == null) {
							itemList[j] = item;
							break;
						} else {
							itemList[j].crashcount = itemList[j].crashcount + 1;
						}
					}
				} else {
					itemList[item.hashnum] = item;
				}
				number = number + 1;

			}
			count = count + pro.pagenum;
		}
		for (int i = 0; i < itemList.length; i++) {
			if (itemList[i] != null) {
				Vector hang = new Vector();
				hang.add(itemList[i].item_id);
				hang.add(itemList[i].pid);
				hang.add(itemList[i].pageid);
				hang.add(itemList[i].physicid);
				hang.add(itemList[i].hashnum);
				hang.add(itemList[i].crashcount);
				hang.add(itemList[i].state);
				hang.add(itemList[i].bitcount);
				item_model.addRow(hang);
			}
		}
	}

	// ������ɽ��̶�Ԫ�飬���̺�pid,���̿ռ�blocksize��
	public void createProcess() {
		int pnumber = random.nextInt(10) + 10;// ���̸���
		for (int i = 0; i < pnumber; i++) {
			process pro = new process();
			pro.pid = i + 1;
			pro.pagenum = random.nextInt(4) + 4;
			pro.blocksize = pagesize * (pro.pagenum);
			pro.hexagon = Integer.toHexString(pro.blocksize * (2 << 13));
			List.addFirst(pro);
		}
	}

	// Hash������
	public int hash(int pid, int pagesize, int p) {
		hashnum = (pid * pagesize + p);
		return hashnum;
	}

	public static void main(String[] args) {
		new operationSystems();

	}

}
