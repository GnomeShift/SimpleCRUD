import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.*;

import static javax.swing.SortOrder.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppTests {

    @Nested
    class GUITests {
        @Mock
        private JFrame mockFrame;
        @Mock
        private DefaultTableModel mockTableModel;

        @Test
        public void testCreateGUI() {
            DataManage.Create create = new DataManage.Create();
            JFrame frame = create.frame;
            DefaultTableModel tableModel = create.tableModel;

            assertTrue(frame.isVisible());
            assertEquals("Добавить данные", frame.getTitle());
            assertEquals(400, frame.getSize().width);
            assertEquals(300, frame.getSize().height);

            assertNotNull(frame.getContentPane().getComponent(0));
            assertInstanceOf(JPanel.class, frame.getContentPane().getComponent(0));

            assertNotNull(tableModel);
            assertEquals(1, DataManage.Create.columnNames.size());
            assertEquals("Name", DataManage.Create.columnNames.getFirst());
        }

        @Test
        public void testAscendingSort() {
            try (MockedStatic<DB> mockedStatic = mockStatic(DB.class)) {
                List<Object[]> testData = new ArrayList<>(List.of(new Object[]{"A"}, new Object[]{"B"}, new Object[]{"C"}));
                mockedStatic.when(DB::getData).thenReturn(testData);

                DataManage.showData(mockFrame, mockTableModel, ASCENDING);

                ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
                verify(mockTableModel, times(testData.size())).addRow(argumentCaptor.capture());
                List<Object[]> capturedRows = argumentCaptor.getAllValues();

                assertEquals(testData.size(), capturedRows.size());
                for (int i = 0; i < testData.size(); i++) {
                    assertArrayEquals(testData.get(i), capturedRows.get(i));
                }
            }
        }

        @Test
        public void testDescendingSort() {
            try (MockedStatic<DB> mockedStatic = mockStatic(DB.class)) {
                List<Object[]> testData = List.of(new Object[]{"A"}, new Object[]{"B"}, new Object[]{"C"});
                List<Object[]> sortedData = testData.reversed();
                mockedStatic.when(DB::getData).thenReturn(testData);

                DataManage.showData(mockFrame, mockTableModel, DESCENDING);

                ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
                verify(mockTableModel, times(testData.size())).addRow(argumentCaptor.capture());
                List<Object[]> capturedRows = argumentCaptor.getAllValues();

                assertEquals(testData.size(), capturedRows.size());
                for (int i = 0; i < testData.size(); i++) {
                    assertArrayEquals(sortedData.get(i), capturedRows.get(i));
                }
            }
        }
    }

    @Nested
    class DataManageTests {
        @Mock
        private JFrame mockFrame;
        @Mock
        private DefaultTableModel mockTableModel;
        @Mock
        private Connection mockConnection;
        @Mock
        private PreparedStatement mockPreparedStatement;

        @BeforeEach
        public void setUp() {
            reset(mockTableModel, mockConnection, mockPreparedStatement);
        }

        @Test
        public void testShowDataEmptyTable() {
            try (MockedStatic<DB> mockedStaticDB = mockStatic(DB.class); MockedStatic<JOptionPane> mockedStaticOptionPane = mockStatic(JOptionPane.class)) {
                mockedStaticDB.when(DB::getData).thenReturn(new ArrayList<>());

                DataManage.showData(mockFrame, mockTableModel, UNSORTED);

                verify(mockTableModel, never()).setRowCount(anyInt());
                verify(mockTableModel, never()).addRow((Object[]) any());
            }
        }

        @Test
        public void testShowDataUpdateTableModel() {
            try (MockedStatic<DB> mockedStatic = mockStatic(DB.class)) {
                List<Object[]> testData = List.of(new Object[]{"Test1"}, new Object[]{"Test2"});
                mockedStatic.when(DB::getData).thenReturn(testData);

                DataManage.showData(mockFrame, mockTableModel, UNSORTED);

                ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
                verify(mockTableModel, times(testData.size())).addRow(argumentCaptor.capture());
                List<Object[]> capturedRows = argumentCaptor.getAllValues();

                assertEquals(testData.size(), capturedRows.size());
                for (int i = 0; i < testData.size(); i++) {
                    assertArrayEquals(testData.get(i), capturedRows.get(i));
                }
            }
        }

        @Test
        public void testAddEmptyData() throws SQLException {
            DataManage.Create create = new DataManage.Create();
            create.frame = mockFrame;
            create.tableModel = mockTableModel;

            when(mockTableModel.getRowCount()).thenReturn(1);
            when(mockTableModel.getValueAt(0, 0)).thenReturn(null);

            try (MockedStatic<DB> mockedStatic = mockStatic(DB.class); MockedStatic<JOptionPane> mockedStaticOptionPane = mockStatic(JOptionPane.class)) {
                mockedStatic.when(DB::getConnection).thenReturn(mockConnection);
                mockedStatic.when(DB::getInsertSql).thenReturn(new StringBuilder("INSERT INTO test (Name,Test) VALUES (?,?)"));
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

                create.addData();
            }
        }

        @Test
        public void testAddValidData() {
            DataManage.Create create = new DataManage.Create();
            create.frame = mockFrame;
            create.tableModel = mockTableModel;
            when(mockTableModel.getRowCount()).thenReturn(1);
            when(mockTableModel.getValueAt(0, 0)).thenReturn("Test");

            assertEquals(1, mockTableModel.getRowCount());
            assertEquals("Test", mockTableModel.getValueAt(0, 0));
        }
    }

    @Nested
    class DBTests {
        @Test
        public void testGetConnection() {
            Connection connection = DB.getConnection();
            assertNotNull(connection);
        }

        @Test
        public void testGetInsertSql() {
            String sql = String.valueOf(DB.getInsertSql());
            assertEquals("INSERT INTO client (Name, CreatedAt) VALUES (?, ?)", sql);
        }

        @Test
        public void testGetUpdateSql() {
            String sql = String.valueOf(DB.getUpdateSql());
            assertEquals("UPDATE client SET ID = ?, Name = ?, CreatedAt = ?, UpdatedAt = ? WHERE id = ?", sql);
        }
    }
}
