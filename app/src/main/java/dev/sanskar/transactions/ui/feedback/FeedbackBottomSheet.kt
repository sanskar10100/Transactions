package dev.sanskar.transactions.ui.feedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sanskar.transactions.*
import dev.sanskar.transactions.databinding.FragmentFeedbackBottomSheetBinding

class FeedbackBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFeedbackBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedbackBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewVersionCode.text = "Version Name: $VERSION_NAME"
        binding.textViewVersionName.text = "Version Code: $VERSION_CODE"
        binding.textViewDeviceInfo.text = "Device Info: $DEVICE_INFO"

        binding.buttonCopy.setOnClickListener {
            context?.copyToClipboard("Build Info", getFeedbackInfo())
        }

        binding.buttonGithub.setOnClickListener {
            val uri = Uri.parse(ISSUE_URL)
                .buildUpon()
                .appendQueryParameter("body", getFeedbackInfo())
                .build()
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(requireContext(), uri)
        }

        binding.buttonGmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("sanskar10100@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback - Transactions")
                putExtra(Intent.EXTRA_TEXT, getFeedbackInfo())
            }
            startActivity(Intent.createChooser(intent, "Send Feedback"))
        }
    }
}