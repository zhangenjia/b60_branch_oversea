package com.adayo.app.music.data.repository;

import android.util.Pair;

import com.adayo.app.music.data.constant.MusicConst;
import com.adayo.app.music.data.response.DataResult;
import com.adayo.app.music.data.response.ResponseInfo;
import com.adayo.proxy.media.api.callback.BreakStateCallback;
import com.adayo.proxy.media.api.callback.DeviceInfoCallback;
import com.adayo.proxy.media.api.callback.ParseCallback;
import com.adayo.proxy.media.api.callback.PlayInfoCallback;
import com.adayo.proxy.media.api.callback.PlayerInfoCallback;
import com.adayo.proxy.media.api.callback.QueryListCallback;
import com.adayo.proxy.media.api.callback.QueryListDoubleCallback;
import com.adayo.proxy.media.bean.FileInfo;
import com.adayo.proxy.media.bean.MetadataInfo;
import com.adayo.proxy.media.bean.NodeInfo;
import com.adayo.proxy.media.constant.MediaConst;
import com.adayo.proxy.media.manager.AudioFunManager;
import com.lt.library.util.LogUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataRepo {
    private final PlayerInfoCallback mPlayerConfigCallback = new PlayerInfoCallbackImpl();
    private final PlayInfoCallback mPlayInfoCallback = new PlayInfoCallbackImpl();
    private final BreakStateCallback mBreakStateCallback = new BreakStateCallbackImpl();
    private final DeviceInfoCallback mDeviceInfoCallback = new DeviceInfoCallbackImpl();
    private final List<DataResult.Result<Integer>> mRepeatModeResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<NodeInfo>> mNodeInfoResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<MetadataInfo>> mMetadataInfoResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<Integer>> mPlayStateResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<Integer>> mPlayErrorCauseResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<Long>> mDurationResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<Long>> mProgressResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<Integer>> mBreakStateResults = new CopyOnWriteArrayList<>();
    private final List<DataResult.Result<Pair<Integer, Integer>>> mDeviceInfoResults = new CopyOnWriteArrayList<>();
    private Integer mRepeatMode;
    private NodeInfo mNodeInfo;
    private MetadataInfo mMetadataInfo;
    private Integer mPlayState;
    private Integer mPlayErrorCause;
    private Long mDuration;
    private Long mProgress;
    private Integer mBreakState;
    private Pair<Integer, Integer> mDeviceInfo;

    {
        AudioFunManager.getFunction().bindPlayerInfoListener(MusicConst.AUDIO_ZONE_CODE, mPlayerConfigCallback);
        AudioFunManager.getFunction().bindPlayInfoListener(MusicConst.AUDIO_ZONE_CODE, mPlayInfoCallback);
        AudioFunManager.getFunction().bindBreakStateListener(MusicConst.AUDIO_ZONE_CODE, mBreakStateCallback);
        AudioFunManager.getFunction().bindDeviceInfoListener(mDeviceInfoCallback);
    }

    private DataRepo() {
    }

    public static DataRepo getInstance() {
        return DataRepoHolder.INSTANCE;
    }

    public void needRepeatMode(DataResult.Result<Integer> repeatModeResult) {
        needDataInternal(mRepeatModeResults, repeatModeResult, mRepeatMode);
    }

    public void needNodeInfo(DataResult.Result<NodeInfo> nodeInfoResult) {
        needDataInternal(mNodeInfoResults, nodeInfoResult, mNodeInfo);
    }

    public void needMetadataInfo(DataResult.Result<MetadataInfo> metadataInfoResult) {
        needDataInternal(mMetadataInfoResults, metadataInfoResult, mMetadataInfo);
    }

    public void needPlayState(DataResult.Result<Integer> playStateResult) {
        needDataInternal(mPlayStateResults, playStateResult, mPlayState);
    }

    public void needPlayErrorCause(DataResult.Result<Integer> playErrorCauseResult) {
        needDataInternal(mPlayErrorCauseResults, playErrorCauseResult, mPlayErrorCause);
    }

    public void needDuration(DataResult.Result<Long> durationResult) {
        needDataInternal(mDurationResults, durationResult, mDuration);
    }

    public void needProgress(DataResult.Result<Long> progressResult) {
        needDataInternal(mProgressResults, progressResult, mProgress);
    }

    public void needBreakState(DataResult.Result<Integer> breakStateResult) {
        needDataInternal(mBreakStateResults, breakStateResult, mBreakState);
    }

    public void needDeviceInfo(DataResult.Result<Pair<Integer, Integer>> deviceInfoResult) {
        needDataInternal(mDeviceInfoResults, deviceInfoResult, mDeviceInfo);
    }

    public void needFileInfos(@MediaConst.DevicePortDef int devicePort, DataResult.Result<List<FileInfo>> result) {
        AudioFunManager.getFunction().queryFileInfos(devicePort, new QueryListCallback<FileInfo>() {
            @Override
            public void onSuccess(List<FileInfo> list) {
                if (mDeviceInfo.second == MediaConst.DeviceStageDef.NULL || mDeviceInfo.second == MediaConst.DeviceStageDef.FIND_UNMOUNTED) {
                    result.onResult(new DataResult<>(null, new ResponseInfo.Builder(false).build()));
                } else {
                    result.onResult(new DataResult<>(list, new ResponseInfo.Builder(true).build()));
                }
            }

            @Override
            public void onFailure(int code) {
                result.onResult(new DataResult<>(null, new ResponseInfo.Builder(false).setResponseCode(code).build()));
            }
        });
    }

    public void needFolderAndFileInfos(String folderPath, DataResult.Result<Pair<List<NodeInfo>, List<FileInfo>>> result) {
        AudioFunManager.getFunction().queryFolderAndFileInfos(folderPath, new QueryListDoubleCallback<NodeInfo, FileInfo>() {
            @Override
            public void onSuccess(List<NodeInfo> list1, List<FileInfo> list2) {
                result.onResult(new DataResult<>(new Pair<>(list1, list2), new ResponseInfo.Builder(true).build()));
            }

            @Override
            public void onFailure(int code) {
                result.onResult(new DataResult<>(null, new ResponseInfo.Builder(false).setResponseCode(code).build()));
            }
        });
    }

    public void needMetadataInfo(String filePath, DataResult.Result<MetadataInfo> result) {
        AudioFunManager.getFunction().parseMetadataInfo(filePath, new ParseCallback<MetadataInfo>() {
            @Override
            public void onSuccess(MetadataInfo metadataInfo) {
                result.onResult(new DataResult<>(metadataInfo, new ResponseInfo.Builder(true).build()));
            }

            @Override
            public void onFailure(int code) {
                result.onResult(new DataResult<>(null, new ResponseInfo.Builder(false).setResponseCode(code).build()));
            }
        });
    }

    private <T> void needDataInternal(List<DataResult.Result<T>> results, DataResult.Result<T> result, T data) {
        synchronized (results) {
            if (results.contains(result)) {
                LogUtil.w("listener repeat bound");
            } else {
                results.add(result);
            }
            if (data != null) {
                result.onResult(new DataResult<>(data, new ResponseInfo.Builder(true).build()));
            }
        }
    }

    private <T> void callOnChangedInternal(List<DataResult.Result<T>> results, T data) {
        synchronized (results) {
            for (DataResult.Result<T> result1 : results) {
                result1.onResult(new DataResult<>(data, new ResponseInfo.Builder(true).build()));
            }
        }
    }

    private static class DataRepoHolder {
        private static final DataRepo INSTANCE = new DataRepo();
    }

    private class PlayerInfoCallbackImpl implements PlayerInfoCallback {
        @Override
        public void onRepeatModeChanged(int repeatMode) {
            mRepeatMode = repeatMode;
            callOnChangedInternal(mRepeatModeResults, repeatMode);
        }
    }

    private class PlayInfoCallbackImpl implements PlayInfoCallback {
        @Override
        public void onNodeInfoChanged(NodeInfo nodeInfo) {
            mNodeInfo = nodeInfo;
            callOnChangedInternal(mNodeInfoResults, nodeInfo);
        }

        @Override
        public void onMetadataInfoChanged(MetadataInfo metadataInfo) {
            mMetadataInfo = metadataInfo;
            callOnChangedInternal(mMetadataInfoResults, metadataInfo);
        }

        @Override
        public void onPlayStateChanged(int playState) {
            mPlayState = playState;
            callOnChangedInternal(mPlayStateResults, playState);
        }

        @Override
        public void onPlayErrorCauseChanged(int playErrorCause) {
            mPlayErrorCause = playErrorCause;
            callOnChangedInternal(mPlayErrorCauseResults, playErrorCause);
        }

        @Override
        public void onDurationChanged(long duration) {
            mDuration = duration;
            callOnChangedInternal(mDurationResults, duration);
        }

        @Override
        public void onProgressChanged(long progress) {
            mProgress = progress;
            callOnChangedInternal(mProgressResults, progress);
        }
    }

    private class BreakStateCallbackImpl implements BreakStateCallback {
        @Override
        public void onChanged(int breakState) {
            mBreakState = breakState;
            callOnChangedInternal(mBreakStateResults, breakState);
        }
    }

    private class DeviceInfoCallbackImpl implements DeviceInfoCallback {
        @Override
        public void onChanged(int devicePort, int deviceStage) {
            if (MediaConst.DevicePortDef.EXTERNAL_USB_1 != devicePort) {
                return;
            }
            Pair<Integer, Integer> deviceInfo = Pair.create(devicePort, deviceStage);
            mDeviceInfo = deviceInfo;
            callOnChangedInternal(mDeviceInfoResults, deviceInfo);
        }
    }
}
