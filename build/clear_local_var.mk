########################################
# clear_local_var.mk
#

LOCAL_API_REV := android-22
LOCAL_BUILDER_REV := $(shell \ls $(ANDROID_HOME)/build-tools | sort -nr | head -1)

LOCAL_DEP_JAR :=
LOCAL_DEP_LIB :=
LOCAL_DEP_PKG :=
LOCAL_DEP_RES :=
LOCAL_DEP_RES1 :=

LOCAL_RES_D := ./res
LOCAL_RES_F :=

LOCAL_SRC_D := ./src
LOCAL_SRC_F :=

LOCAL_AMF_F := ./AndroidManifest.xml
LOCAL_PKG_S :=

LOCAL_OUT_D := ./out

LOCAL_IS_LIB := false
