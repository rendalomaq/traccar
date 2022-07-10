package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolTest;

public class FutureWayFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new FutureWayFrameDecoder());

        verifyFrame(
                binary("343130303030303039424130303030344750533a562c3230303930323039333333332c302e3030303030304e2c302e303030303030452c302e3030302c302e3030300d0a574946493a332c317c39302d36372d31432d46372d32312d36437c353226327c38302d38392d31372d43362d37392d41307c353426337c34302d46342d32302d45462d44442d32417c35380d0a4c42533a3436302c302c34363437353036362c36390d0a36413432"),
                decoder.decode(null, null, binary("343130303030303039424130303030344750533a562c3230303930323039333333332c302e3030303030304e2c302e303030303030452c302e3030302c302e3030300d0a574946493a332c317c39302d36372d31432d46372d32312d36437c353226327c38302d38392d31372d43362d37392d41307c353426337c34302d46342d32302d45462d44442d32417c35380d0a4c42533a3436302c302c34363437353036362c36390d0a36413432")));

        verifyFrame(
                binary("34313030303030303346323030303032302c494d45493a3335343832383130303132363436312c62617474657279206c6576656c3a362c6e6574776f726b20747970653a372c4353513a323336463432"),
                decoder.decode(null, null, binary("34313030303030303346323030303032302c494d45493a3335343832383130303132363436312c62617474657279206c6576656c3a362c6e6574776f726b20747970653a372c4353513a323336463432")));

    }

}
