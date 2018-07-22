package 操作系统;

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

//进程类；
class process {
	int pid;// 进程号
	int blocksize;// 逻辑空间大小
	int pagenum;// 逻辑页面数
	String hexagon;// 进程逻辑地址
}

// 页表项类； 
class pgtabitem {
	int item_id;// 页表项序号
	int hashnum;// 散列值
	int pid;// 进程号
	int pageid;// 页面号
	int physicid;// 页框号
	int crashcount;// 冲突计数
	int state;// 空闲占用标志
	int bitcount;// 空间大小
}

public class operationSystems extends JFrame {
	Container contentPane = getContentPane();
	JButton submit = new JButton("创建进程");
	JButton clear = new JButton("清空释放内存");
	JTable itemTable;
	JTable processTable;
	JTable processList;
	JPanel choose = new JPanel();
	DefaultTableModel item_model = new DefaultTableModel();
	DefaultTableModel processList_model = new DefaultTableModel();
	JComboBox<String> physicalSpaceCombo = new JComboBox<String>(new String[] { "256MB", "512MB" });
	JComboBox<String> pageSizeCombo = new JComboBox<String>(new String[] { "1KB", "2KB", "4KB" });
	Random random = new Random();
	int hashnum;// 散列值；
	int pagesize;// 页面大小，又GUI手动输入；
	String physicsize;// 物理内存大小，由GUI选择输入；
	LinkedList<process> List = new LinkedList<process>();// 自动生成进程链表；
	pgtabitem[] itemList = new pgtabitem[2 << 19];// 已分配页表项数组；
	Vector process_col = new Vector();
	Vector item_col = new Vector();

	operationSystems() {
		super("倒置页表的模拟实现");
		contentPane.add(choose, BorderLayout.NORTH);
		choose.add(physicalSpaceCombo);
		choose.add(pageSizeCombo);
		choose.add(submit);
		choose.add(clear);
		process_col.add("进程号");
		process_col.add("逻辑空间大小/KB");
		process_col.add("可分配物理空间");
		process_col.add("逻辑地址");
		item_col.add("页表项序号");
		item_col.add("进程号");
		item_col.add("逻辑页面号");
		item_col.add("页框号");
		item_col.add("散列值");
		item_col.add("冲突计数");
		item_col.add("占用标志:1→占用 /0→空闲");
		item_col.add("页表项所占空间");
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

	// 清空函数：clear();
	public void clear() {
		List.clear();
		for (int i = 0; i < itemList.length; i++) {
			itemList[i] = null;
		}
	}

	// submit按鈕功能函數；
	public void run() {
		createProcess();
		createItemTable();
		createProcessList();
	}

	// 将进程压入表格中
	public void createProcessList() {
		for (Iterator<process> it = List.iterator(); it.hasNext();) {
			process pro = it.next();
			Vector hang = new Vector();
			hang.add(pro.pid);// 压入进程号
			hang.add(pro.blocksize);// 顺序分配逻辑页面
			hang.add(physicsize);// 可分配物理空间
			hang.add("0XFFFFFFFFF" + pro.hexagon);
			processList_model.addRow(hang);// 在散列值的位置压入页表项；
		}

	}

	// 把页表项放入表格中
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
				// 判断散列值是否冲突；
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

	// 随机生成进程二元组，进程号pid,进程空间blocksize；
	public void createProcess() {
		int pnumber = random.nextInt(10) + 10;// 进程个数
		for (int i = 0; i < pnumber; i++) {
			process pro = new process();
			pro.pid = i + 1;
			pro.pagenum = random.nextInt(4) + 4;
			pro.blocksize = pagesize * (pro.pagenum);
			pro.hexagon = Integer.toHexString(pro.blocksize * (2 << 13));
			List.addFirst(pro);
		}
	}

	// Hash函数；
	public int hash(int pid, int pagesize, int p) {
		hashnum = (pid * pagesize + p);
		return hashnum;
	}

	public static void main(String[] args) {
		new operationSystems();

	}

}
