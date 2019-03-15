import cv2
import numpy as np
import math
from enum import Enum
from networktables import NetworkTables

class GripPipeline:
    """
    An OpenCV pipeline generated by GRIP.
    """

    def __init__(self):
        """initializes all values to presets or None if need to be set
        """

        self.__hsv_threshold_hue = [0.0, 180.0]
        self.__hsv_threshold_saturation = [0.0, 19.04923599320881]
        self.__hsv_threshold_value = [0.0, 255.0]

        self.hsv_threshold_output = None

        self.__cv_erode_src = self.hsv_threshold_output
        self.__cv_erode_kernel = None
        self.__cv_erode_anchor = (-1, -1)
        self.__cv_erode_iterations = 5.0
        self.__cv_erode_bordertype = cv2.BORDER_CONSTANT
        self.__cv_erode_bordervalue = (-1)

        self.cv_erode_output = None

        self.__find_contours_input = self.cv_erode_output
        self.__find_contours_external_only = False

        self.find_contours_output = None

    def process(self, source0):
        """
        Runs the pipeline and sets all outputs to new values.
        """
        # Step HSV_Threshold0:
        self.__hsv_threshold_input = source0
        (self.hsv_threshold_output) = self.__hsv_threshold(self.__hsv_threshold_input, self.__hsv_threshold_hue,
                                                           self.__hsv_threshold_saturation, self.__hsv_threshold_value)

        # Step CV_erode0:
        self.__cv_erode_src = self.hsv_threshold_output
        (self.cv_erode_output) = self.__cv_erode(self.__cv_erode_src, self.__cv_erode_kernel, self.__cv_erode_anchor,
                                                 self.__cv_erode_iterations, self.__cv_erode_bordertype,
                                                 self.__cv_erode_bordervalue)

        # Step Find_Contours0:
        self.__find_contours_input = self.cv_erode_output
        (self.find_contours_output) = self.__find_contours(self.__find_contours_input,
                                                           self.__find_contours_external_only)
        return self.cv_erode_output, self.find_contours_output

    @staticmethod
    def __hsv_threshold(input, hue, sat, val):
        """Segment an image based on hue, saturation, and value ranges.
        Args:
            input: A BGR numpy.ndarray.
            hue: A list of two numbers the are the min and max hue.
            sat: A list of two numbers the are the min and max saturation.
            lum: A list of two numbers the are the min and max value.
        Returns:
            A black and white numpy.ndarray.
        """
        out = cv2.cvtColor(input, cv2.COLOR_BGR2HSV)
        return cv2.inRange(out, (hue[0], sat[0], val[0]), (hue[1], sat[1], val[1]))

    @staticmethod
    def __cv_erode(src, kernel, anchor, iterations, border_type, border_value):
        """Expands area of lower value in an image.
        Args:
           src: A numpy.ndarray.
           kernel: The kernel for erosion. A numpy.ndarray.
           iterations: the number of times to erode.
           border_type: Opencv enum that represents a border type.
           border_value: value to be used for a constant border.
        Returns:
            A numpy.ndarray after erosion.
        """
        return cv2.erode(src, kernel, anchor, iterations=(int)(iterations + 0.5),
                         borderType=border_type, borderValue=border_value)

    @staticmethod
    def __find_contours(input, external_only):
        """Sets the values of pixels in a binary image to their distance to the nearest black pixel.
        Args:
            input: A numpy.ndarray.
            external_only: A boolean. If true only external contours are found.
        Return:
            A list of numpy.ndarray where each one represents a contour.
        """
        if (external_only):
            mode = cv2.RETR_EXTERNAL
        else:
            mode = cv2.RETR_LIST
        method = cv2.CHAIN_APPROX_SIMPLE
        im2, contours, hierarchy = cv2.findContours(input, mode=mode, method=method)
        return contours

cam = cv2.VideoCapture(0)

img_counter = 0

NetworkTables.initialize()
sd = NetworkTables.getTable("SmartDashboard")

r = 10
D = 10
w = 1

while True:
    ret, frame = cam.read()
    # cv2.imshow("test", frame)
    if not ret:
        break
    k = cv2.waitKey(1)

    if k%256 == 27:
        # ESC pressed
        print("Escape hit, closing...")
        break
    elif k%256 == 32:
        # SPACE pressed
        img_name = "opencv_frame_{}.png".format(img_counter)
        cv2.imwrite(img_name, frame)
        print("{} written!".format(img_name))
        img_counter += 1

    pipe = GripPipeline()
    img, cntrs = pipe.process(source0=frame)

    if len(cntrs) > 1:
        cmap = []
        for c in cntrs:
            area = cv2.contourArea(c)
            cmap.append((area, c))

        cmap.sort(key=lambda x: x[0])
        big_cntrs = []
        big_cntrs.append(cmap[len(cmap)-2][1])
        big_cntrs.append(cmap[len(cmap)-1][1])

        ccX = 0
        ccY = 0
        cX = []
        cY = []

        for c in big_cntrs:
            # compute the center of the contour
            M = cv2.moments(c)
            x = int(M["m10"] / M["m00"])
            y = int(M["m01"] / M["m00"])

            cX.append(x)
            cY.append(y)

            # draw the contour and center of the shape on the image
            cv2.drawContours(img, [c], -1, (0, 255, 0), 2)
            cv2.circle(img, (x, y), 7, (128, 128, 128), -1)

        ccX = int(np.mean(cX))
        ccY = int(np.mean(cY))
        cv2.circle(img, (ccX, ccY), 7, (128, 128, 128), -1)

        dif = ccX - img.shape[0]/2;
        # go left is negative, go right is positive
        print(dif)

        # send T to Roborio
        sd.putNumber("AlignDif", dif)

    # show the image
    cv2.imshow("Image", img)

cam.release()

cv2.destroyAllWindows()

