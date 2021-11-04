package scanisette.tools;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;


public class KeyHookTool {
    private static HHOOK hhk;
    private static LowLevelKeyboardProc keyboardHook;
    private static User32 lib;

    // https://stackoverflow.com/questions/51446208/how-would-i-change-this-jna-hook-to-listen-to-a-specific-key-versus-all-keys
    // https://docs.microsoft.com/en-us/windows/desktop/inputdev/virtual-key-codes
    public static void blockWindowsKey() {
        if (isWindows()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    lib = User32.INSTANCE;
                    HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
                    keyboardHook = new LowLevelKeyboardProc() {
                        public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
                            if (nCode >= 0) {
                                switch (info.vkCode){
                                    case 0x5B:  //WINDOWS KEY
                                    case 0x5C: //WINDOWS KEY
                                    case 0x10: //SHIFT
                                    case 0x11:  // CONTROL
                                    case 0x12: // ALT
                                    case 0x1B:  //ESCAPE
                                        return new LRESULT(1);
                                    default: //do nothing
                                }
                            }
                            Pointer ptr = info.getPointer();
                            long peer = Pointer.nativeValue(ptr);
                            return lib.CallNextHookEx(hhk, nCode, wParam, new WinDef.LPARAM(peer));
                        }
                    };
                    hhk = lib.SetWindowsHookEx(13, keyboardHook, hMod, 0);

                    // This bit never returns from GetMessage
                    int result;
                    MSG msg = new MSG();
                    while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
                        if (result == -1) {
                            break;
                        } else {
                            lib.TranslateMessage(msg);
                            lib.DispatchMessage(msg);
                        }
                    }
                    lib.UnhookWindowsHookEx(hhk);
                }
            }).start();
        }
    }

    public static void unblockWindowsKey() {
        if (isWindows() && lib != null) {
            lib.UnhookWindowsHookEx(hhk);
        }
    }

    public static boolean isWindows(){
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf( "win" ) >= 0);
    }
}

