package org.example.uelearner.devices;

import org.example.uelearner.ue.UESUL;

public class DeviceSULFactory {
    public static DeviceSUL getSULByDevice(String deviceName, UESUL uesul) {
        System.out.println(deviceName + " start!");
        if (deviceName.equals("Huawei")) {
            System.out.println("Huawei initialized!");
            return new FGSUL(uesul);
        } else if (deviceName.equals("Rog")) {
            System.out.println("Rog initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Oppo")) {
            System.out.println("Oppo initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Motorola")) {
            System.out.println("Motorola initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("iphone")) {
            System.out.println("iphone initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Quectel")) {
            System.out.println("Quectel initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("SamsungS20")) {
            System.out.println("SamsungS20 initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("OneplusNord")) {
            System.out.println("OneplusNord initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Oneplus10pro")) {
            System.out.println("Oneplus10pro initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("BlackShark")) {
            System.out.println("BlackShark initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Hisense")) {
            System.out.println("Hisense initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Nothing")) {
            System.out.println("Nothing initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("RedMagic")) {
            System.out.println("RedMagic initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Redmi")) {
            System.out.println("Redmi initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Pixel7")) {
            System.out.println("Pixel7 initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("DPixel7")) {
            System.out.println("DPixel7 initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("Pixel6")) {
            System.out.println("Pixel6 initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("DPixel6")) {
            System.out.println("DPixel6 initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("srsue1")) {
            System.out.println("srsue1 initialized!");
            return new srsueSUL(uesul);
        }else if (deviceName.equals("srsue")) {
            System.out.println("srsue initialized!");
            return new srsueSUL(uesul);
        }else if (deviceName.equals("Dsrsue")) {
            System.out.println("Dsrsue initialized!");
            return new srsueSUL(uesul);
        }else if (deviceName.equals("oaiue")) {
            System.out.println("oaiue initialized!");
            return new oaiueSUL(uesul);
        }else if (deviceName.equals("Doaiue")) {
            System.out.println("Doaiue initialized!");
            return new oaiueSUL(uesul);
        }else if (deviceName.equals("SamsungS21")) {
            System.out.println("SamsungS21 initialized!");
            return new FGSUL(uesul);
        }else if (deviceName.equals("DSamsungS21")) {
            System.out.println("DSamsungS21 initialized!");
            return new FGSUL(uesul);
        }
        return null;
    }
}
