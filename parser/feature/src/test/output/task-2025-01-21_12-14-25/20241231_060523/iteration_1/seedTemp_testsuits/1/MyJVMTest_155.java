import java.awt.Component;

public class MyJVMTest_155 {

    static javax.swing.JSpinner spTimeoutHours = null;

    static javax.swing.JCheckBox cbTimeoutEnabled = null;

    static javax.swing.JLabel lbTimeoutHours = null;

    javax.swing.JLabel setCheckboxEnabledState(javax.swing.JSpinner spTimeoutHours, javax.swing.JCheckBox cbTimeoutEnabled, javax.swing.JLabel lbTimeoutHours) throws Exception {
        if (cbTimeoutEnabled.isEnabled() && cbTimeoutEnabled.isSelected()) {
            spTimeoutHours.setEnabled(true);
            lbTimeoutHours.setEnabled(true);
        } else {
            spTimeoutHours.setEnabled(false);
            lbTimeoutHours.setEnabled(false);
        }
        return lbTimeoutHours;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_155().setCheckboxEnabledState(spTimeoutHours, cbTimeoutEnabled, lbTimeoutHours);
    }
}
